// Copyright 2000-2018 JetBrains s.r.o.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
package org.jetbrains.plugins.ruby.motion;

import com.intellij.execution.RunManager;
import com.intellij.execution.RunnerAndConfigurationSettings;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.StartupManager;
import com.intellij.openapi.util.Key;
import com.intellij.util.ArrayUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.ruby.RBundle;
import org.jetbrains.plugins.ruby.motion.bridgesupport.Framework;
import org.jetbrains.plugins.ruby.tasks.rake.RakeUtilBase;
import org.jetbrains.plugins.ruby.tasks.rake.runConfigurations.RakeRunConfigurationType;
import org.jetbrains.plugins.ruby.util.ProjectQueues;

import java.util.Collection;
import java.util.Collections;

/**
 * @author Anna Bulenkova
 */
public class RubyMotionUtilExt {
  protected static final Key<Collection<Framework>> FRAMEWORKS_LIST = Key.create("ruby.motion.frameworks.list");

  private static RunnerAndConfigurationSettings createAndAddRakeConfiguration(final String taskFullName,
                                                                              final Module module,
                                                                              final RunManager runManager) {
    final RunnerAndConfigurationSettings settings =
      RakeRunConfigurationType.getInstance().getRakeFactory().createConfigurationSettings(module, taskFullName,
                                                                                          ArrayUtil.EMPTY_STRING_ARRAY,
                                                                                          Collections.emptyMap());
    runManager.addConfiguration(settings);
    return settings;
  }

  public static void createMotionRunConfiguration(final Module module) {
    final Project project = module.getProject();

    StartupManager.getInstance(project).runWhenProjectIsInitialized(() -> ProjectQueues.getInstance(project).queue(
      RBundle.message("rails.facet.builder.run.configuration.server.creating"),
      new Task.Backgroundable(project, RBundle.message("rails.facet.builder.run.configuration.server.creating.for", module.getName()),
                              true) {
        public void run(@NotNull ProgressIndicator indicator) {
          indicator.setText(RBundle.message("progress.backgnd.indicator.title.please.wait", getTitle()));

          final RunManager runManager = RunManager.getInstance(project);

          ApplicationManager.getApplication().runReadAction(() -> {
            // requires read action

            // Rake : "simulator"
            final String taskName = RubyMotionUtil.getInstance().getMainRakeTask(module);
            final RunnerAndConfigurationSettings simulator = createAndAddRakeConfiguration(taskName, module, runManager);

            // Rake : "spec"
            createAndAddRakeConfiguration(RakeUtilBase.TASKS_SPEC_FULLCMD, module, runManager);

            // make development config active
            runManager.setSelectedConfiguration(simulator);
          });
        }
      }));
  }

}
