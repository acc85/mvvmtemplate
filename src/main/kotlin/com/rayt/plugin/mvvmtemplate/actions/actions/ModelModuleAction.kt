package com.rayt.plugin.mvvmtemplate.actions.actions

import com.android.tools.idea.flags.StudioFlags
import com.android.tools.idea.gradle.project.importing.AndroidGradleProjectImportProvider
import com.android.tools.idea.gradle.project.sync.ng.nosyncbuilder.newfacade.androidproject.NewAndroidProject
import com.android.tools.idea.gradle.project.sync.ng.nosyncbuilder.proto.AndroidProjectProto
import com.android.tools.idea.npw.ideahost.AndroidModuleBuilder
import com.android.tools.idea.npw.model.NewProjectModel
import com.android.tools.idea.npw.model.RenderTemplateModel
import com.android.tools.idea.npw.project.ChooseAndroidProjectStep
import com.android.tools.idea.npw.project.TestStep
import com.android.tools.idea.npw.project.deprecated.ConfigureAndroidProjectStep
import com.android.tools.idea.sdk.wizard.SdkQuickfixUtils
import com.android.tools.idea.ui.wizard.StudioWizardDialogBuilder
import com.android.tools.idea.wizard.model.ModelWizard
import com.android.tools.idea.wizard.model.ModelWizardStep
import com.intellij.ide.projectWizard.NewProjectWizard
import com.intellij.ide.util.newProjectWizard.AddModuleWizard
import com.intellij.ide.util.projectWizard.WizardContext
import com.intellij.idea.ActionsBundle
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
import org.jetbrains.android.sdk.AndroidSdkUtils
import org.jetbrains.android.util.AndroidBundle

class ModelModuleAction : AnAction() {

    private val TAG = ModelModuleAction::class.java.simpleName

    private val KEY_R_ANDROID = "r_android"

    private var myCurrentProject: Project? = null
    private var myDataContext: DataContext? = null
    private var myTemplateModel: RenderTemplateModel? = null
    private var myAndroidFacet: AndroidFacet? = null


    override fun actionPerformed(e: AnActionEvent) {

        val myCurrentProject = e.getData(PlatformDataKeys.PROJECT)
        val basePath: String? = myCurrentProject!!.basePath
        funTestNewProject(e)
//        funAndroidNewProject(e)

    }

    fun funTestNewProject(e:AnActionEvent){
        if (!AndroidSdkUtils.isAndroidSdkAvailable()) {
            SdkQuickfixUtils.showSdkMissingDialog()
        } else {
            val projectModel = NewProjectModel()
            var wizard: ModelWizard? = null
            val style: StudioWizardDialogBuilder.UxStyle

            wizard = ModelWizard.Builder(*arrayOfNulls(0)).addStep(TestModel(projectModel, "test")).build()
            style = StudioWizardDialogBuilder.UxStyle.INSTANT_APP

//            wizard!!.addResultListener(object : ModelWizard.WizardListener {
//                override fun onWizardFinished(result: ModelWizard.WizardResult) {
//                    projectModel.onWizardFinished(result)
//                }
//            })
            wizard!!.goForward()
            StudioWizardDialogBuilder(wizard, ActionsBundle.actionText("WelcomeScreen.CreateNewProject")).setUxStyle(
                style
            ).build().show()
        }

    }


    fun funAndroidNewProject(e:AnActionEvent){
        if (!AndroidSdkUtils.isAndroidSdkAvailable()) {
            SdkQuickfixUtils.showSdkMissingDialog()
        } else {
            val projectModel = NewProjectModel()
            var wizard: ModelWizard? = null
            val style: StudioWizardDialogBuilder.UxStyle
            if (StudioFlags.NPW_DYNAMIC_APPS.get() as Boolean) {
                wizard = ModelWizard.Builder(*arrayOfNulls(0)).addStep(ChooseAndroidProjectStep(projectModel)).build()
                style = StudioWizardDialogBuilder.UxStyle.DYNAMIC_APP
            } else {
                wizard =
                    ModelWizard.Builder(*arrayOfNulls(0)).addStep(ConfigureAndroidProjectStep(projectModel)).build()
                style = StudioWizardDialogBuilder.UxStyle.INSTANT_APP
            }

            wizard!!.addResultListener(object : ModelWizard.WizardListener {
                override fun onWizardFinished(result: ModelWizard.WizardResult) {
                    projectModel.onWizardFinished(result)
                }
            })
            StudioWizardDialogBuilder(wizard, ActionsBundle.actionText("WelcomeScreen.CreateNewProject")).setUxStyle(
                style
            ).build().show()
        }

    }

    fun runNewProjectWizard(e:AnActionEvent){
        val myCurrentProject = e.getData(PlatformDataKeys.PROJECT)
        val basePath: String? = myCurrentProject!!.basePath
        val projectName: String? = myCurrentProject.name
        var moduleBuilder = AndroidModuleBuilder()
        var newProjectWizard = NewProjectWizard(myCurrentProject,ModulesProvider.EMPTY_MODULES_PROVIDER,basePath!!)
        val projectBuilder = newProjectWizard.projectBuilder
        projectBuilder.commit(myCurrentProject, null, ModulesProvider.EMPTY_MODULES_PROVIDER);
    }

    fun runNewModuleWizardInstant(e: AnActionEvent){
        val myCurrentProject = e.getData(PlatformDataKeys.PROJECT)
        val jdk: Sdk? = ProjectRootManager.getInstance(myCurrentProject!!).getProjectSdk()
        val basePath: String? = myCurrentProject.basePath
        val projectName: String? = myCurrentProject.name
        val addModuleWizard = AddModuleWizard(null, basePath!!, getProjectProvider())
        val wizardContext: WizardContext = addModuleWizard.getWizardContext();
        wizardContext.projectJdk = jdk
        wizardContext.projectName = projectName
        wizardContext.compilerOutputDirectory =
            CompilerProjectExtension.getInstance(myCurrentProject)?.compilerOutputUrl
        val projectBuilder = addModuleWizard.projectBuilder
        projectBuilder.commit(myCurrentProject, null, ModulesProvider.EMPTY_MODULES_PROVIDER);
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