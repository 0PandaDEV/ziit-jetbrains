package net.pandadev.ziitjetbrains

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.event.*
import com.intellij.openapi.fileEditor.FileDocumentManagerListener
import com.intellij.openapi.fileEditor.FileEditorManagerEvent
import com.intellij.openapi.fileEditor.FileEditorManagerListener
import com.intellij.openapi.vfs.VirtualFile

class Listeners {
    class SaveListener : FileDocumentManagerListener {
        override fun beforeDocumentSaving(document: Document) {
            try {
                val file = getFileFromDocument(document) ?: return
                heartbeatSend(file)
            } catch (e: Exception) {
                LogService.getInstance().error("Error in SaveListener: ${e.message}")
            }
        }
    }

    class EditorMouseListener : com.intellij.openapi.editor.event.EditorMouseListener {
        override fun mousePressed(event: EditorMouseEvent) {
            try {
                val document = event.editor.document
                val file = getFileFromDocument(document) ?: return

                ApplicationManager.getApplication().invokeLater {
                    heartbeatSend(file)
                }
            } catch (e: Exception) {
                LogService.getInstance().error("Error in EditorMouseListener: ${e.message}")
            }
        }

        override fun mouseClicked(event: EditorMouseEvent) {}
        override fun mouseReleased(event: EditorMouseEvent) {}
        override fun mouseEntered(event: EditorMouseEvent) {}
        override fun mouseExited(event: EditorMouseEvent) {}
    }

    class VisibleAreaListener : com.intellij.openapi.editor.event.VisibleAreaListener {
        override fun visibleAreaChanged(event: VisibleAreaEvent) {
            try {
                if (!didChange(event)) return

                val document = event.editor.document
                val file = getFileFromDocument(document) ?: return

                heartbeatSend(file)
            } catch (e: Exception) {
                LogService.getInstance().error("Error in VisibleAreaListener: ${e.message}")
            }
        }

        private fun didChange(event: VisibleAreaEvent): Boolean {
            val oldRect = event.oldRectangle ?: return true
            val newRect = event.newRectangle
            return newRect.x != oldRect.x || newRect.y != oldRect.y
        }
    }

    class CaretListener : com.intellij.openapi.editor.event.CaretListener {
        override fun caretPositionChanged(event: CaretEvent) {
            try {
                val editor = event.editor
                val document = editor.document
                val file = getFileFromDocument(document) ?: return

                ApplicationManager.getApplication().invokeLater {
                    heartbeatSend(file)
                }
            } catch (e: Exception) {
                LogService.getInstance().error("Error in CaretListener: ${e.message}")
            }
        }
    }

    class DocumentListener : BulkAwareDocumentListener.Simple {
        override fun documentChangedNonBulk(event: DocumentEvent) {
            try {
                val document = event.document
                val file = getFileFromDocument(document) ?: return

                heartbeatSend(file)
            } catch (e: Exception) {
                LogService.getInstance().error("Error in DocumentListener: ${e.message}")
            }
        }
    }

    class FileEditorListener : FileEditorManagerListener {
        override fun selectionChanged(event: FileEditorManagerEvent) {
            try {
                val file = event.newFile ?: return

                heartbeatSend(file)
            } catch (e: Exception) {
                LogService.getInstance().error("Error in FileEditorListener: ${e.message}")
            }
        }
    }

    companion object {
        private val heartbeatService by lazy { HeartbeatService.getInstance() }
        
        fun getFileFromDocument(document: Document): VirtualFile? {
            val fileDocManager = com.intellij.openapi.fileEditor.FileDocumentManager.getInstance()
            return fileDocManager.getFile(document)
        }

        fun heartbeatSend(file: VirtualFile) {
            heartbeatService.handleFileChange(file)
        }
    }
} 