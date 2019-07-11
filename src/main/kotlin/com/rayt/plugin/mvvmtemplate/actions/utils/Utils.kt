package com.rayt.plugin.mvvmtemplate.actions.utils

import com.intellij.notification.NotificationDisplayType
import com.intellij.notification.NotificationGroup
import com.intellij.notification.NotificationType
import com.intellij.openapi.application.PathManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.wm.ToolWindowId

import javax.swing.*
import java.awt.*
import java.io.File

/**
 * Created by andrealucibello on 28/02/17.
 */
object Utils {

    val PLUGIN_DIRECTORY_NAME = "Droidcon-Plugin-Example"

    private val TOOLWINDOW_NOTIFICATION = NotificationGroup.toolWindowGroup(
        "Toolwindow",
        ToolWindowId.VCS,
        true
    )

    private val STICKY_NOTIFICATION = NotificationGroup(
        "Sticky",
        NotificationDisplayType.STICKY_BALLOON,
        true
    )

    private val BALLOON_NOTIFICATION = NotificationGroup(
        "Baloon",
        NotificationDisplayType.BALLOON,
        true
    )


    /**
     * Returns the directory in which all the files relative to the plugin reside.
     *
     * @return the plugin directory if it exists and is writable, null otherwise
     */
    val pluginDirectory: File?
        get() {
            val myPluginDirectory: File
            val pluginsDirectory = File(PathManager.getPluginsPath())
            if (pluginsDirectory.isDirectory) {
                myPluginDirectory = File(pluginsDirectory.path + File.separator + PLUGIN_DIRECTORY_NAME)
                if (myPluginDirectory.exists() && myPluginDirectory.isDirectory && myPluginDirectory.canExecute() && myPluginDirectory.canWrite()) {
                    return myPluginDirectory
                }
            }
            return null
        }


    /**
     * Gets the directory with the templates.
     *
     * @return template directory if exists and is readable, null otherwise
     */
    val templateDirectory: File?
        get() {
            val pluginDirectory = pluginDirectory
            if (pluginDirectory == null || !pluginDirectory.exists()) {
                return null
            }
            val templateDirectory = File(pluginDirectory, "templates")
            return if (templateDirectory == null || !templateDirectory.exists() || !templateDirectory.canRead()) {
                null
            } else templateDirectory
        }

    private fun notify(
        type: NotificationType,
        group: NotificationGroup,
        project: Project,
        title: String,
        message: String
    ) {
        group.createNotification(title, message, type, null).notify(project)
    }

    fun notifySuccess(project: Project, title: String, message: String) {
        notify(NotificationType.INFORMATION, STICKY_NOTIFICATION, project, title, message)
    }

    fun showDialogMessage(project: Project, title: String, message: String) {
        Messages.showMessageDialog(project, message, title, Messages.getInformationIcon())
    }

    fun showCustomDialogMessage(project: Project, title: String, message: String) {

        val wrapper = object : DialogWrapper(project, false) {

            init {
                init()
            }

            override fun createCenterPanel(): JComponent? {
                val panel = JPanel(BorderLayout())
                //                final JTextArea textArea = new JTextArea("some string");
                //                textArea.setEditable(false);
                //                textArea.setRows(40);
                //                textArea.setColumns(70);
                val label = JLabel(message)
                panel.add(label)
                //                panel.add(ScrollPaneFactory.createScrollPane(textArea));
                return panel
            }
        }
        wrapper.centerRelativeToParent()
        wrapper.title = title
        wrapper.show()
    }
}
