package com.rayt.plugin.mvvmtemplate.actions.actions

import com.android.tools.idea.npw.cpp.ConfigureCppSupportStep
import com.android.tools.idea.npw.model.NewProjectModel
import com.android.tools.idea.npw.model.NewProjectModuleModel
import com.android.tools.idea.npw.project.ConfigureAndroidProjectStep
import com.android.tools.idea.npw.template.ConfigureTemplateParametersStep
import com.android.tools.idea.projectsystem.NamedModuleTemplate
import com.android.tools.idea.wizard.model.ModelWizardStep
import com.android.tools.idea.wizard.model.WizardModel
import com.intellij.util.containers.ContainerUtil.newArrayList
import org.jetbrains.android.util.AndroidBundle.message
import javax.swing.JComponent
import javax.swing.JPanel

class TestModel(m:NewProjectModel,title:String): ModelWizardStep<NewProjectModel>(m,title) {

    lateinit var myNewProjectModuleModel:NewProjectModuleModel

    init{
        model.projectLocation().set("C:\\TestingProject")
        model.applicationName().set("TestApplication")
        model.companyDomain().set("example.com")
        model.companyDomain().set("com.example.TestApplication")
        model.enableCppSupport().set(false)

    }

    lateinit var myPanel:JPanel

    override fun getComponent(): JComponent {
        return myPanel
    }


    override fun createDependentSteps(): Collection<ModelWizardStep<*>> {
        myNewProjectModuleModel = NewProjectModuleModel(model)
        val renderModel = myNewProjectModuleModel.getExtraRenderTemplateModel()

        return newArrayList<ModelWizardStep<out WizardModel>>(
            ConfigureAndroidProjectStep(myNewProjectModuleModel, model),
            ConfigureCppSupportStep(model),
            ConfigureTemplateParametersStep(
                renderModel,
                message("android.wizard.config.activity.title"),
                newArrayList<NamedModuleTemplate>()
            )
        )
    }


}