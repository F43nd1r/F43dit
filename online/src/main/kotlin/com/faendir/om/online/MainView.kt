package com.faendir.om.online

import com.faendir.om.dsl.DslCompiler
import com.faendir.om.dsl.DslGenerator
import com.faendir.om.sp.SolutionParser
import com.juicy.JuicyAceEditor
import com.juicy.theme.JuicyAceTheme
import com.vaadin.flow.component.AttachEvent
import com.vaadin.flow.component.Text
import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.dialog.Dialog
import com.vaadin.flow.component.html.Anchor
import com.vaadin.flow.component.notification.Notification
import com.vaadin.flow.component.notification.NotificationVariant
import com.vaadin.flow.component.orderedlayout.FlexLayout
import com.vaadin.flow.component.orderedlayout.VerticalLayout
import com.vaadin.flow.component.page.Push
import com.vaadin.flow.component.progressbar.ProgressBar
import com.vaadin.flow.component.upload.Upload
import com.vaadin.flow.component.upload.receivers.MemoryBuffer
import com.vaadin.flow.router.Route
import com.vaadin.flow.server.InputStreamFactory
import com.vaadin.flow.server.StreamResource
import com.vaadin.flow.spring.annotation.SpringComponent
import com.vaadin.flow.spring.annotation.UIScope
import com.vaadin.flow.theme.lumo.Lumo
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.io.streams.asInput
import kotlinx.io.streams.asOutput
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import kotlin.script.experimental.api.valueOrNull

@UIScope
@SpringComponent
@Push
@Route("")
class MainView : FlexLayout() {
    var currentFileName: String = "generated.solution"

    init {
        setSizeFull()
        val editor = JuicyAceEditor()
        editor.element.setAttribute("mode", "ace/mode/kotlin")
        editor.setTheme(JuicyAceTheme.ambiance)
        editor.setHeightFull()
        expand(editor)
        val sidebar = VerticalLayout()
        sidebar.setHeightFull()
        sidebar.width = null
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
        sidebar.add(upload)
        val downloadDialog = Button("Download solution...") {
            val dialog = Dialog(Text("Please wait while your solution is generated"), ProgressBar().apply { isIndeterminate = true })
            dialog.open()
            GlobalScope.launch {
                if (editor.value == null) {
                    dialog.removeAll()
                    dialog.add(Text("Your solution is empty."))
                } else {
                    try {
                        val result = DslCompiler.fromDsl(editor.value)
                        val solution = result.valueOrNull()
                        if (solution != null) {
                            val out = ByteArrayOutputStream()
                            SolutionParser.write(solution, out.asOutput())
                            ui.ifPresent {
                                it.access {
                                    val download = Anchor(StreamResource(currentFileName, InputStreamFactory { ByteArrayInputStream(out.toByteArray()) }), "")
                                    download.add(Button("Download") { dialog.close() }.apply { setWidthFull() })
                                    download.element.setAttribute("download", true)
                                    dialog.removeAll()
                                    dialog.add(download)
                                }
                            }
                        } else {
                            ui.ifPresent {
                                it.access {
                                    dialog.removeAll()
                                    val reportString = result.reports.joinToString("\n") { it.exception?.toString() ?: it.message }
                                    dialog.add(Text(reportString))
                                    println(reportString)
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
                    }
                }
            }
        }
        sidebar.add(downloadDialog)
        add(sidebar, editor)
    }

    override fun onAttach(attachEvent: AttachEvent?) {
        ui.orElse(null)?.element?.setAttribute("theme", Lumo.DARK)
        super.onAttach(attachEvent)
    }
}