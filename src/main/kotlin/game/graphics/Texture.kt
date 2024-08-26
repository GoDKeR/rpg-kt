package org.godker.rpg.game.graphics

import org.godker.rpg.game.resources.Resource
import org.godker.rpg.game.resources.ResourceManager
import org.godker.rpg.game.resources.ResourceType
import org.lwjgl.opengl.GL11.*
import org.lwjgl.opengl.GL13.GL_CLAMP_TO_BORDER
import org.lwjgl.stb.STBImage.stbi_failure_reason
import org.lwjgl.stb.STBImage.stbi_set_flip_vertically_on_load
import org.lwjgl.system.MemoryStack
import java.nio.ByteBuffer


class Texture (val texId: Int, val width: Int, val height: Int, val data: ByteBuffer) {
    val id: Int = glGenTextures()

    init {
        bind()

        setParam(GL_TEXTURE_WRAP_S, GL_CLAMP_TO_BORDER)
        setParam(GL_TEXTURE_WRAP_T, GL_CLAMP_TO_BORDER)
        setParam(GL_TEXTURE_MIN_FILTER, GL_NEAREST)
        setParam(GL_TEXTURE_MAG_FILTER, GL_NEAREST)

        uploadData(GL_RGBA8, width, height, GL_RGBA, data)
    }

    fun bind(){
        glBindTexture(GL_TEXTURE_2D, id)
    }

    fun setParam(name: Int, value: Int) {
        glTexParameteri(GL_TEXTURE_2D, name, value)
    }

    fun uploadData(width: Int, height: Int, data: ByteBuffer) {
        uploadData(GL_RGBA8, width, height, GL_RGBA, data)
    }

    fun uploadData(internalFormat: Int, width: Int, height: Int, format: Int, data: ByteBuffer){
        glTexImage2D(GL_TEXTURE_2D, 0, internalFormat, width, height, 0, format, GL_UNSIGNED_BYTE, data)
    }

    fun delete(){
        glDeleteTextures(id)
    }

}

fun loadTexture(path: String) : Texture? {
    val resource = ResourceManager.getResource<Resource.ImageResource>("/graficos/$path.png", ResourceType.IMAGE)

    val width: Int = resource.width
    val height: Int = resource.height
    val data: ByteBuffer = resource.data ?: return null

    return Texture(path.toInt(),width, height, data)
}