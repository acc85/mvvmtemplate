package com.rayt.plugin.mvvmtemplate.actions.actions

import com.android.tools.idea.gradle.project.importing.AndroidGradleProjectImportProvider
import com.android.tools.idea.npw.model.RenderTemplateModel
import com.android.tools.idea.wizard.model.ModelWizard
import com.intellij.ide.util.newProjectWizard.AddModuleWizard
import com.intellij.ide.util.projectWizard.WizardContext
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.actionSystem.PlatformDataKeys
import com.intellij.openapi.extensions.Extensions
import com.intellij.openapi.project.Project
import com.intellij.openapi.projectRoots.Sdk
import com.intellij.openapi.roots.CompilerProjectExtension
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.openapi.roots.ui.configuration.ModulesProvider
import com.intellij.projectImport.ProjectImportProvider
import org.jetbrains.android.facet.AndroidFacet

class ModelModuleAction : AnAction() {

    private val TAG = ModelModuleAction::class.java.simpleName

    private val KEY_R_ANDROID = "r_android"

    private var myCurrentProject: Project? = null
    private var myDataContext: DataContext? = null
    private var myTemplateModel: RenderTemplateModel? = null
    private var myAndroidFacet: AndroidFacet? = null


    override fun actionPerformed(e: AnActionEvent) {
        val myCurrentProject = e.getData(PlatformDataKeys.PROJECT)
        val jdk: Sdk? = ProjectRootManager.getInstance(myCurrentProject!!).getProjectSdk()
        val basePath: String? = myCurrentProject.basePath
        val projectName: String? = myCurrentProject.name
        val addModuleWizard = AddModuleWizard(null, basePath!!, getProjectProvider())
        val wizardContext: WizardContext = addModuleWizard.getWizardContext();
        wizardContext.projectJdk = jdk;
        wizardContext.projectName = projectName;
        wizardContext.compilerOutputDirectory =
            CompilerProjectExtension.getInstance(myCurrentProject)?.compilerOutputUrl
        val projectBuilder = addModuleWizard.projectBuilder
        projectBuilder.commit(myCurrentProject, null, ModulesProvider.EMPTY_MODULES_PROVIDER);
//        var myProject: Project? = TestProjectBuilder().createProject("new Project", "C:\\plugins\\test")
    }


    fun getProjectProvider(): AndroidGradleProjectImportProvider? {
        for (p: ProjectImportProvider in Extensions.getExtensions(ProjectImportProvider.PROJECT_IMPORT_PROVIDER)) {
            if (p as? AndroidGradleProjectImportProvider != null) {
                return p
            }
        }
        return null
    }

        /**
         * This one dictates the action applied to the generated files after the wizard has finished.
         * Here we move the two method from the fragment file to the modules and component files in injection directory.
         */
        private inner class MyResultListener : ModelWizard.WizardListener {

            fun onWizardFinished(result: Boolean) {
                // do something after wizard has finished
            }
        }
    }