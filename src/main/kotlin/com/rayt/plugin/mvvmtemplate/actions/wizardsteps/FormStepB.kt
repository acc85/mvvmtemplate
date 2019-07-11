package com.rayt.plugin.mvvmtemplate.actions.wizardsteps

import com.android.tools.adtui.util.FormScalingUtil
import com.android.tools.adtui.validation.ValidatorPanel
import com.android.tools.idea.observable.BindingsManager
import com.android.tools.idea.ui.wizard.StudioWizardStepPanel

import javax.swing.*

class FormStepB(override var myModel: DroidWizardModel) : FormStep(myModel, WIZARD_STEP_TITLE) {

    private val myRootPanel: StudioWizardStepPanel

    private val myValidatorPanel: ValidatorPanel

    private var numberB: JTextField? = null

    private var myPanel: JPanel? = null

    private val myBindings = BindingsManager()


    init {
        myValidatorPanel = ValidatorPanel(this, myPanel!!)
        myRootPanel = StudioWizardStepPanel(myValidatorPanel)
        FormScalingUtil.scaleComponentTree(this.javaClass, myRootPanel)
    }

    override fun onProceeding() {
        super.onProceeding()
        myModel.secondNumber = Integer.valueOf(numberB!!.text)
    }


    override fun getComponent(): JComponent {
        return myRootPanel
    }

    companion object {

        private val WIZARD_STEP_TITLE = "Create Droidcon Android Project"
    }
}
