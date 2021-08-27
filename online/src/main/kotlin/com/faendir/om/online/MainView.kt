package com.faendir.om.online

import com.faendir.om.dsl.DslGenerator
import com.faendir.om.dsl.api.ArmRepresentation
import com.faendir.om.dsl.api.Step
import com.faendir.om.dsl.api.Tape
import com.faendir.om.online.remote.RemoteResult
import com.faendir.om.online.remote.RemoteServer
import com.faendir.om.parser.solution.SolutionParser
import com.faendir.om.parser.solution.model.part.*
import com.vaadin.flow.component.AttachEvent
import com.vaadin.flow.component.Text
import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.dialog.Dialog
import com.vaadin.flow.component.html.Anchor
import com.vaadin.flow.component.html.Div
import com.vaadin.flow.component.html.Image
import com.vaadin.flow.component.notification.Notification
import com.vaadin.flow.component.notification.NotificationVariant
import com.vaadin.flow.component.orderedlayout.FlexLayout
import com.vaadin.flow.component.orderedlayout.VerticalLayout
import com.vaadin.flow.component.page.AppShellConfigurator
import com.vaadin.flow.component.page.Push
import com.vaadin.flow.component.progressbar.ProgressBar
import com.vaadin.flow.component.upload.Upload
import com.vaadin.flow.component.upload.receivers.MemoryBuffer
import com.vaadin.flow.router.PageTitle
import com.vaadin.flow.router.Route
import com.vaadin.flow.server.AppShellSettings
import com.vaadin.flow.server.InputStreamFactory
import com.vaadin.flow.server.StreamResource
import com.vaadin.flow.server.VaadinSession
import com.vaadin.flow.spring.annotation.SpringComponent
import com.vaadin.flow.theme.lumo.Lumo
import de.f0rce.ace.AceEditor
import de.f0rce.ace.enums.AceMode
import de.f0rce.ace.enums.AceTheme
import okio.buffer
import okio.source
import org.springframework.beans.factory.config.ConfigurableBeanFactory
import org.springframework.context.annotation.Scope
import java.io.ByteArrayInputStream
import java.security.PrivilegedActionException
import java.util.*
import java.util.concurrent.TimeoutException

@PageTitle("F43dit")
@Scope(scopeName = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@SpringComponent
@Push
@Route("")
class MainView : FlexLayout(), AppShellConfigurator {
    private var currentFileName: String = "generated.solution"
    private val editor = AceEditor().apply {
        theme = AceTheme.ambiance
        mode = AceMode.kotlin
        isAutoComplete = true
        isLiveAutocompletion = true
        setCustomAutoCompletion((
                listOf(Arm::class, Conduit::class, Glyph::class, IO::class, Track::class)
                    .flatMap { type -> type.java.declaredFields.map { it.name } + type.simpleName!!.lowercase() }
                        + ArmType.values().map { it.name }
                        + GlyphType.values().map { it.name }
                        + IOType.values().map { it.name }
                        + listOf(Tape::class, Step::class, ArmRepresentation::class)
                    .flatMap { type -> type.java.declaredMethods.map { it.name } }.minus("wait0")
                        + listOf("tape", "to", "listOf")
                ).toSet().toTypedArray(), true)
        isEnableSnippets = true
        style["font"] = "monospace"
        fontSize = 16
        setHeightFull()
        expand(this)
    }
    private val downloadText = Anchor().apply {
        setWidthFull()
        add(Button("Download text").apply { setWidthFull() })
    }
    private val uploadContainer = Div().apply { setWidthFull() }

    init {
        setSizeFull()
        val logo = Image("logo.png", "F43dit")
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
                                            dialog.add(Button("Close") { dialog.close() }.apply { setWidthFull() })
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
        val sidebar = VerticalLayout(logo, uploadContainer, downloadDialog, downloadText, spacer, help)
        sidebar.expand(spacer)
        sidebar.setHeightFull()
        sidebar.width = null
        add(sidebar, editor)
    }

    private fun updateDownloadText() {
        downloadText.setHref(StreamResource("$currentFileName.kts", InputStreamFactory { ByteArrayInputStream(editor.value.toByteArray()) }))
    }

    override fun onAttach(attachEvent: AttachEvent?) {
        ui.orElse(null)?.element?.setAttribute("theme", Lumo.DARK)
        super.onAttach(attachEvent)

        val buffer = MemoryBuffer()
        val upload = Upload(buffer)
        upload.setWidthFull()
        upload.style["box-sizing"] = "border-box"
        upload.addFinishedListener {
            try {
                if (it.fileName.endsWith(".solution")) {
                    val solution = SolutionParser.parse(buffer.inputStream.source().buffer())
                    editor.value = DslGenerator.toDsl(solution)
                    currentFileName = it.fileName
                } else if (it.fileName.endsWith(".solution.kts")) {
                    editor.value = buffer.inputStream.bufferedReader().readText()
                    currentFileName = it.fileName.removeSuffix(".kts")
                }
                updateDownloadText()
            } catch (e: Exception) {
                e.printStackTrace()
                Notification.show("Failed to parse file.").addThemeVariants(NotificationVariant.LUMO_ERROR)
            }
        }
        uploadContainer.removeAll()
        uploadContainer.add(upload)

        updateDownloadText()
    }

    override fun configurePage(settings: AppShellSettings) {
        settings.loadingIndicatorConfiguration.ifPresent {
            it.firstDelay = 2000
            it.secondDelay = 5000
            it.thirdDelay = 10000
        }
    }
}

object HasActiveRequestMarker