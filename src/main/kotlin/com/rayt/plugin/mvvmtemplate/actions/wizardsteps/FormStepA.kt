package com.rayt.plugin.mvvmtemplate.actions.wizardsteps

import com.android.tools.adtui.util.FormScalingUtil
import com.android.tools.adtui.validation.ValidatorPanel
import com.android.tools.idea.ui.wizard.StudioWizardStepPanel

import javax.swing.*

class FormStepA(model: DroidWizardModel) : FormStep(model, WIZARD_STEP_TITLE) {

    private val myRootPanel: StudioWizardStepPanel
    private val myValidatorPanel: ValidatorPanel
    private var numberA: JTextField? = null
    private var myPanel: JPanel? = null


    init {
        myValidatorPanel = ValidatorPanel(this, myPanel!!)
        //        myRootPanel = new StudioWizardStepPanel(myValidatorPanel, "Configure your new project");
        myRootPanel = StudioWizardStepPanel(myValidatorPanel)
        FormScalingUtil.scaleComponentTree(this.javaClass, myRootPanel)
    }


    override fun onProceeding() {
        super.onProceeding()
        myModel.firstNumber = Integer.valueOf(numberA!!.text)
    }


    override fun getComponent(): JComponent {
        return myRootPanel
    }

    companion object {

        private val WIZARD_STEP_TITLE = "Create Droidcon Android Project"
    }

}
