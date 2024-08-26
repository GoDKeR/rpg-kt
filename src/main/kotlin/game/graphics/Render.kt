package org.godker.rpg.game.graphics


import org.godker.rpg.game.resources.Resource
import org.godker.rpg.game.resources.ResourceManager
import org.godker.rpg.game.resources.ResourceType
import org.godker.rpg.math.Matrix4f
import org.lwjgl.glfw.GLFW.glfwGetCurrentContext
import org.lwjgl.glfw.GLFW.glfwGetFramebufferSize
import org.lwjgl.opengl.GL30.*
import org.lwjgl.system.MemoryStack
import org.lwjgl.system.MemoryUtil
import java.nio.FloatBuffer

private data class DrawableElement(
    val tex: Int,
    val x: Float,
    val y: Float,
    val w: Float,
    val h: Float,
    val color: Color,
    val tx1: Float,
    val ty1: Float,
    val tx2: Float,
    val ty2: Float
)

class Render {
    val MAX_SPRITES: Int = 4000

    var vertexBufferObject: Int = 0
    var vertexArrayObject: Int = 0
    var shaderProgram: Int = 0

    var vertexShader: Int = 0
    var fragmentShader: Int = 0

    lateinit var vertices: FloatBuffer

    var numVertices = 0
    var numSprites = 0

    var currentTexture: Texture? = null

    private val drawables = arrayOfNulls<DrawableElement>(MAX_SPRITES)

    init {
        try {
            vertexBufferObject = glGenBuffers()
            vertexArrayObject = glGenVertexArrays()

            glBindVertexArray(vertexArrayObject)
            glBindBuffer(GL_ARRAY_BUFFER, vertexBufferObject)

            vertices = MemoryUtil.memAllocFloat(MAX_SPRITES * 6 * 8)

            glBufferData(GL_ARRAY_BUFFER, (vertices.capacity()).toLong(), GL_DYNAMIC_DRAW)

            numVertices = 0

            vertexShader = glCreateShader(GL_VERTEX_SHADER)
            fragmentShader = glCreateShader(GL_FRAGMENT_SHADER)

            glShaderSource(
                vertexShader,
                ResourceManager.getResource<Resource.ShaderResource>("/shaders/vertex.glsl", ResourceType.SHADER).data
            )
            glShaderSource(
                fragmentShader,
                ResourceManager.getResource<Resource.ShaderResource>("/shaders/fragment.glsl", ResourceType.SHADER).data
            )

            glCompileShader(vertexShader)

            if (glGetShaderi(vertexShader, GL_COMPILE_STATUS) != GL_TRUE) {
                throw RuntimeException("Unable to compile vertex shader.")
            }

            glCompileShader(fragmentShader)

            if (glGetShaderi(fragmentShader, GL_COMPILE_STATUS) != GL_TRUE) {
                throw RuntimeException("Unable to compile fragment shader.")
            }

            shaderProgram = glCreateProgram()
            glAttachShader(shaderProgram, vertexShader)
            glAttachShader(shaderProgram, fragmentShader)

            glBindFragDataLocation(shaderProgram, 0, "fragColor")
            glLinkProgram(shaderProgram)

            if (glGetProgrami(shaderProgram, GL_LINK_STATUS) != GL_TRUE) {
                throw RuntimeException("Unable to link program.")
            }

            glUseProgram(shaderProgram)

            glDeleteShader(vertexShader)
            glDeleteShader(fragmentShader)

            val window = glfwGetCurrentContext()
            var width = 0
            var height = 0

            MemoryStack.stackPush().use { stack ->
                val widthBuffer = stack.mallocInt(1)
                val heightBuffer = stack.mallocInt(1);

                glfwGetFramebufferSize(window, widthBuffer, heightBuffer)

                width = widthBuffer.get()
                height = heightBuffer.get()
            }

            var location: Int = glGetAttribLocation(shaderProgram, "position")
            glEnableVertexAttribArray(location)
            glVertexAttribPointer(location, 2, GL_FLOAT, false, 8 * Float.SIZE_BYTES, 0)

            location = glGetAttribLocation(shaderProgram, "color")
            glEnableVertexAttribArray(location)
            glVertexAttribPointer(location, 4, GL_FLOAT, false, 8 * Float.SIZE_BYTES, (2 * Float.SIZE_BYTES).toLong())

            location = glGetAttribLocation(shaderProgram, "texcoord")
            glEnableVertexAttribArray(location)
            glVertexAttribPointer(location, 2, GL_FLOAT, false, 8 * Float.SIZE_BYTES, (6 * Float.SIZE_BYTES).toLong())

            location = glGetUniformLocation(shaderProgram, "texImage")
            glUniform1i(location, 0)

            val model = Matrix4f()

            location = glGetUniformLocation(shaderProgram, "model")
            MemoryStack.stackPush().use { stack ->
                val buffer: FloatBuffer = stack.mallocFloat(4 * 4)
                model.toBuffer(buffer)
                glUniformMatrix4fv(location, false, buffer)
            }

            val view = Matrix4f()
            location = glGetUniformLocation(shaderProgram, "view")
            MemoryStack.stackPush().use { stack ->
                val buffer: FloatBuffer = stack.mallocFloat(4 * 4)
                view.toBuffer(buffer)
                glUniformMatrix4fv(location, false, buffer)
            }

            val projection = Matrix4f.orthographic(0f, width.toFloat(), 0f, height.toFloat(), -1f, 1f)
            location = glGetUniformLocation(shaderProgram, "projection")
            MemoryStack.stackPush().use { stack ->
                val buffer: FloatBuffer = stack.mallocFloat(4 * 4)
                projection.toBuffer(buffer)
                glUniformMatrix4fv(location, false, buffer)
            }

            glEnable(GL_BLEND)
            glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)

        } catch (e: Exception) {
            println("Exception ${e.message}")
        }
    }

    fun begin() {
        glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)
        numVertices = 0
    }

    fun end() {
        flush()
    }

    fun dispose() {
        MemoryUtil.memFree(vertices)

        glDeleteVertexArrays(vertexArrayObject)
        glDeleteBuffers(vertexBufferObject)
        glDeleteProgram(shaderProgram)
    }

    fun drawTexture(
        texture: Int,
        x: Float,
        y: Float,
        w: Float,
        h: Float,
        sX: Float,
        sY: Float,
        texWidth: Float,
        texHeight: Float,
        color: Color
    ) {

        drawBatch(texture, x, y, w, h, color, sX / texWidth, sY / texHeight, (sX + w) / texWidth, (sY + h) / texHeight)
    }

    private fun drawBatch(
        texture: Int,
        x: Float,
        y: Float,
        w: Float,
        h: Float,
        color: Color,
        tX1: Float,
        tY1: Float,
        tX2: Float,
        tY2: Float
    ) {
        if (numSprites == MAX_SPRITES ) {
            flush()
        }

        drawables[numSprites++] = DrawableElement(texture, x, y, w, h, color, tX1, tY1, tX2, tY2)
    }

    private fun flush() {
        if (numSprites == 0) return

        //Order drawables by filenum

        var lastTextureId = -1

        for (i in 0..<numSprites) {
            val item = drawables[i] ?: break

            if (lastTextureId == -1 && item.tex != 0) {
                lastTextureId = item.tex
                currentTexture = loadTexture(item.tex.toString())
            }

            if (lastTextureId != item.tex && item.tex != 0) {
                write()
                lastTextureId = item.tex
                currentTexture = loadTexture(item.tex.toString())
            }

            val r: Float = item.color.getRed()
            val g: Float = item.color.getGreen()
            val b: Float = item.color.getBlue()
            val a: Float = item.color.getAlpha()

            vertices.put(item.x).put(item.y).put(r).put(g).put(b).put(a).put(item.tx1).put(item.ty1)
            vertices.put(item.x).put(item.y + item.h).put(r).put(g).put(b).put(a).put(item.tx1).put(item.ty2)
            vertices.put(item.x + item.w).put(item.y + item.h).put(r).put(g).put(b).put(a).put(item.tx2).put(item.ty2)

            vertices.put(item.x).put(item.y).put(r).put(g).put(b).put(a).put(item.tx1).put(item.ty1)
            vertices.put(item.x + item.w).put(item.y + item.h).put(r).put(g).put(b).put(a).put(item.tx2).put(item.ty2)
            vertices.put(item.x + item.w).put(item.y).put(r).put(g).put(b).put(a).put(item.tx2).put(item.ty1)

            numVertices += 6
        }

        write()
        numSprites = 0
    }

    private fun write() {
        glBindTexture(GL_TEXTURE_2D, currentTexture!!.id)
        vertices.flip()

        glBindVertexArray(vertexArrayObject)
        glUseProgram(shaderProgram)

        glBindBuffer(GL_ARRAY_BUFFER, vertexBufferObject)
        glBufferSubData(GL_ARRAY_BUFFER, 0, vertices)

        glDrawArrays(GL_TRIANGLES, 0, numVertices)

        vertices.clear()
        currentTexture!!.delete()

        numVertices = 0
    }
}