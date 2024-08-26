package org.godker.rpg.game

import org.godker.rpg.game.graphics.Color
import org.godker.rpg.game.graphics.Render
import org.lwjgl.glfw.Callbacks.glfwFreeCallbacks
import org.lwjgl.glfw.GLFW.*
import org.lwjgl.glfw.GLFWErrorCallback
import org.lwjgl.opengl.GL
import org.lwjgl.opengl.GL11.*
import org.lwjgl.system.MemoryStack.stackPush
import org.lwjgl.system.MemoryUtil.memFree
import java.util.*

class Game {
    private var window: Long = 0

    private lateinit var renderer: Render
    private val map = org.godker.rpg.game.world.Map("isla.tmx")
    fun run() {
        println("Starting LWJGL")

        init()
        loop()

        renderer.dispose()
        destroy()
    }

    private fun init() {
        GLFWErrorCallback.createPrint(System.err).set()

        if (!glfwInit())
            throw IllegalStateException("Cannot initialize GLFW")

        glfwDefaultWindowHints()
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE)
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE)

        this.window = glfwCreateWindow(1366, 768, "RPG Client", 0, 0)

        if (this.window == 0L)
            throw RuntimeException("Failed to create the window")

        glfwSetKeyCallback(this.window) {
                window, key, scancode, action, mods ->
            if (key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE)
                glfwWindowShouldClose(window)
        }

        stackPush().use { stack ->
            val width = stack.mallocInt(1) // int*
            val height = stack.mallocInt(1) // int*

            glfwGetWindowSize(window, width, height)

            val videoMode = glfwGetVideoMode(glfwGetPrimaryMonitor()) ?: throw RuntimeException("Cannot get video mode.")

            // Center the window
            glfwSetWindowPos(
                window,
                (videoMode.width() - width[0]) / 2,
                (videoMode.height() - height[0]) / 2
            )
        }

        glfwMakeContextCurrent(this.window)
        GL.createCapabilities()
        glfwSwapInterval(1)
        glfwShowWindow(this.window)

        renderer = Render()
    }

    private fun loop() {
        GL.createCapabilities()

        glClearColor(0.0f, 0.0f, 0.0f, 1.0f)

        val layers = map.mapLayers

        while (!glfwWindowShouldClose(this.window)) {
            val time: Double = glfwGetTime()

            renderer.begin()

            for(layer in layers) {
                for (y in 0..<24) {
                    for (x in 0..<43) {

                        val gid = layer.tileCoordinates[x][y]

                        if (gid == 0)
                            continue

                        val ts = map.getImageByGid(gid) ?: continue
                        val gidObj = map.gidList[gid - 1]

                        renderer.drawTexture(ts.tileSetImage.texture.toInt(), x * 32f, y * 32f, 32f, 32f, gidObj.sX.toFloat(), gidObj.sY.toFloat(), ts.tileSetImage.imageWidth.toFloat(), ts.tileSetImage.imageHeight.toFloat(), Color.WHITE)
                    }
                }
            }
            renderer.end()

            glfwSwapBuffers(this.window)
            glfwPollEvents()
        }
    }

    private fun destroy() {
        memFree(GL.getCapabilities().addressBuffer)
        GL.setCapabilities(null)

        glfwFreeCallbacks(this.window)
        glfwDestroyWindow(this.window)

        glfwTerminate()

        Objects.requireNonNull(glfwSetErrorCallback(null))?.free();

    }
}