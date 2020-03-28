package com.faendir.om.online

import com.faendir.om.dsl.DslGenerator
import com.faendir.om.online.remote.RemoteResult
import com.faendir.om.online.remote.RemoteServer
import com.faendir.om.sp.SolutionParser
import com.juicy.JuicyAceEditor
import com.juicy.theme.JuicyAceTheme
import com.vaadin.flow.component.AttachEvent
import com.vaadin.flow.component.Text
import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.dialog.Dialog
import com.vaadin.flow.component.html.Anchor
import com.vaadin.flow.component.html.Div
import com.vaadin.flow.component.notification.Notification
import com.vaadin.flow.component.notification.NotificationVariant
import com.vaadin.flow.component.orderedlayout.FlexLayout
import com.vaadin.flow.component.orderedlayout.VerticalLayout
import com.vaadin.flow.component.page.Push
import com.vaadin.flow.component.progressbar.ProgressBar
import com.vaadin.flow.component.upload.Upload
import com.vaadin.flow.component.upload.receivers.MemoryBuffer
import com.vaadin.flow.router.PageTitle
import com.vaadin.flow.router.Route
import com.vaadin.flow.server.*
import com.vaadin.flow.spring.annotation.SpringComponent
import com.vaadin.flow.spring.annotation.UIScope
import com.vaadin.flow.theme.lumo.Lumo
import kotlinx.io.streams.asInput
import java.io.ByteArrayInputStream
import java.security.PrivilegedActionException
import java.util.concurrent.TimeoutException

@PageTitle("Omsekt")
@UIScope
@SpringComponent
@Push
@Route("")
class MainView : FlexLayout(), PageConfigurator {
    private var currentFileName: String = "generated.solution"

    init {
        setSizeFull()
        val editor = JuicyAceEditor()
        editor.element.style["font"] = "monospace"
        editor.element.setAttribute("mode", "ace/mode/kotlin")
        editor.setTheme(JuicyAceTheme.ambiance)
        editor.setFontsize(16)
        editor.setHeightFull()
        expand(editor)
        val buffer = MemoryBuffer()
        val upload = Upload(buffer)
        upload.addFinishedListener {
            try {
                val solution = SolutionParser.parse(buffer.inputStream.asInput())
                editor.value = DslGenerator.toDsl(solution)
                currentFileName = it.fileName
            } catch (e: Exception) {
                Notification.show("Failed to parse file.").addThemeVariants(NotificationVariant.LUMO_ERROR)
            }
        }
        val downloadDialog = Button("Download solution...") {
            val dialog = Dialog(Text("Please wait while your solution is generated"), ProgressBar().apply { isIndeterminate = true })
            dialog.isCloseOnOutsideClick = false
            dialog.open()
            val session = VaadinSession.getCurrent()
            when {
                editor.value == null -> {
                    dialog.removeAll()
                    dialog.add(Text("Your solution is empty."))
                }
                session.getAttribute(HasActiveRequestMarker::class.java) != null -> {
                    dialog.removeAll()
                    dialog.add(Text("Only one request per user at the same time allowed."))
                }
                else -> {
                    session.setAttribute(HasActiveRequestMarker::class.java, HasActiveRequestMarker)
                    RemoteServer.fromDsl(editor.value) { result ->
                        try {
                            when (result) {
                                is RemoteResult.Success -> {
                                    val solution = result.value
                                    ui.ifPresent {
                                        it.access {
                                            val download = Anchor(StreamResource(currentFileName, InputStreamFactory { ByteArrayInputStream(solution) }), "")
                                            download.add(Button("Download") { dialog.close() }.apply { setWidthFull() })
                                            download.element.setAttribute("download", true)
                                            dialog.removeAll()
                                            dialog.add(download)
                                        }
                                    }
                                }
                                is RemoteResult.Failure -> {
                                    ui.ifPresent { ui ->
                                        ui.access {
                                            dialog.removeAll()
                                            dialog.add(
                                                Text(
                                                    when (result.exception) {
                                                        is PrivilegedActionException -> "Illegal action in script."
                                                        is TimeoutException -> "Solution generation timed out."
                                                        is SyntaxException -> "Syntax error: ${result.exception.message} in line ${result.exception.line}"
                                                        else -> "Something went wrong while generating your solution."
                                                    }
                                                )
                                            )
                                        }
                                    }
                                }
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                            ui.ifPresent {
                                it.access {
                                    dialog.removeAll()
                                    dialog.add(Text("Something went wrong while generating your solution."))
                                }
                            }
                        } finally {
                            session.access { session.setAttribute(HasActiveRequestMarker::class.java, null) }
                        }
                    }
                }
            }
        }
        downloadDialog.setWidthFull()
        val help = Anchor("https://github.com/F43nd1r/omsekt/wiki/File-definition", "Reference")
        val spacer = Div()
        val sidebar = VerticalLayout(upload, downloadDialog, spacer, help)
        sidebar.expand(spacer)
        sidebar.setHeightFull()
        sidebar.width = null
        add(sidebar, editor)
    }

    override fun onAttach(attachEvent: AttachEvent?) {
        ui.orElse(null)?.element?.setAttribute("theme", Lumo.DARK)
        super.onAttach(attachEvent)
    }

    override fun configurePage(settings: InitialPageSettings) {
        settings.loadingIndicatorConfiguration.apply {
            firstDelay = 2000
            secondDelay = 5000
            thirdDelay = 10000
        }
    }
}

object HasActiveRequestMarker