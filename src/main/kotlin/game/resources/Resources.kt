package org.godker.rpg.game.resources

import org.lwjgl.stb.STBImage.*
import org.lwjgl.system.MemoryStack
import java.nio.ByteBuffer

enum class ResourceType {
    IMAGE, SOUND, BINARY, SHADER, TILED
}

sealed class Resource {
    abstract val resourcePath: String

    data class ImageResource(override val resourcePath: String, val width: Int, val height: Int, val data: ByteBuffer?) : Resource()
    data class SoundResource(override val resourcePath: String /* TODO */) : Resource()
    data class BinaryResource(override val resourcePath: String, val data: ByteArray) : Resource()
    data class ShaderResource(override val resourcePath: String, val data: CharSequence) : Resource()
    data class TiledResource(override val resourcePath: String, val data: String) : Resource()
}

interface ResourceLoader<T : Resource> {
    fun load(path: String): T
}

class BinaryResourceLoader : ResourceLoader<Resource.BinaryResource> {

    override fun load(path: String): Resource.BinaryResource {
        val data: ByteArray = {}.javaClass.getResource(path)?.readBytes() ?: throw RuntimeException("Unable to load the file located at $path")

        if (data.isEmpty())
            throw RuntimeException("Unable to load the file located at $path")

        return Resource.BinaryResource(path, data)
    }
}

class ImageResourceLoader : ResourceLoader<Resource.ImageResource> {
    override fun load(path: String): Resource.ImageResource {
        var image: ByteBuffer
        var width: Int = 0
        var height: Int = 0

        //TODO: stbi_free somewhere??????

        MemoryStack.stackPush().use { stack ->
            val w = stack.mallocInt(1)
            val h = stack.mallocInt(1)
            val comp = stack.mallocInt(1)

            stbi_set_flip_vertically_on_load(true)

            try {
                image = stbi_load({}.javaClass.getResource(path)!!.file.substring(1), w, h, comp, 4)!!
            }catch (e: Exception){
                println("Error loading image: ${e.message}. ${stbi_failure_reason()}")
                return@use
            }

            width = w.get()
            height = h.get()
            return Resource.ImageResource(path, width, height, image)
        }
        return Resource.ImageResource(path, 0, 0, null)
    }
}

class SoundResourceLoader : ResourceLoader<Resource.SoundResource> {
    override fun load(path: String): Resource.SoundResource {
        return Resource.SoundResource(path)
    }
}

class ShaderResourceLoader : ResourceLoader<Resource.ShaderResource> {
    override fun load(path: String): Resource.ShaderResource {
        val data = {}.javaClass.getResource(path)?.readText() ?: throw RuntimeException("Unable to load the file located at $path")

        if (data.isEmpty())
            throw RuntimeException("Unable to load the file located at $path")

        return Resource.ShaderResource(path, data)
    }
}

class TiledResourceLoader : ResourceLoader<Resource.TiledResource> {
    override fun load(path: String): Resource.TiledResource {
        val data = {}.javaClass.getResource(path)?.readText() ?: throw RuntimeException("Unable to load tmx at $path")

        if (data.isEmpty())
            throw RuntimeException("Unable to read tmx")

        return Resource.TiledResource(path, data)
    }
}

object ResourceManager {
    //TODO: unload unused resources
    private val resources = mutableMapOf<String, Resource>()

    private val loaders = mapOf<ResourceType, ResourceLoader<*>>(
        ResourceType.BINARY to BinaryResourceLoader(),
        ResourceType.IMAGE to ImageResourceLoader(),
        ResourceType.SOUND to SoundResourceLoader(),
        ResourceType.SHADER to ShaderResourceLoader(),
        ResourceType.TILED to TiledResourceLoader()
    )

    @Suppress("UNCHECKED_CAST")
    fun <T : Resource> getResource(path: String, resourceType: ResourceType): T {
        return resources[path] as? T ?: loadResource(path, resourceType)
    }

    private fun <T : Resource> loadResource(path: String, resourceType: ResourceType): T {
        val loader = loaders[resourceType] as ResourceLoader<T>
        val resource = loader.load(path)
        resources[path] = resource
        return resource
    }
}
