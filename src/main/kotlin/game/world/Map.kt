package org.godker.rpg.game.world

import org.godker.rpg.game.resources.Resource
import org.godker.rpg.game.resources.ResourceManager
import org.godker.rpg.game.resources.ResourceType
import org.w3c.dom.Document
import org.xml.sax.InputSource
import org.xml.sax.XMLReader
import java.io.StringReader
import java.util.*
import javax.xml.parsers.DocumentBuilder
import javax.xml.parsers.DocumentBuilderFactory
import kotlin.math.ceil

data class Gid(val gID: Int, val sX: Int, val sY: Int)

data class Layer(
    val layerWidth: Int,
    val layerHeight: Int,
    val layerData: MutableList<UInt> = mutableListOf(),
    val tileCoordinates: Array<Array<Int>>,
    val layerName: String = ""
)

data class TileSetImage(
    val imageWidth: Int,
    val imageHeight: Int,
    val imageSource: String,
    val texture: UInt
)

data class TileSet(
    val firstGid: Int,
    val lastGid: Int,
    val name: String,
    val tileSetImage: TileSetImage,
    val tileWidth: Int,
    val tileHeight: Int,
    val tileCount: Int,
    val columns: Int,
    val tileAmountWidth: Int
)

class Map(val fileName: String) {

    var mapWith: Int = 0
    var mapHeight: Int = 0
    var tileWidth: Int = 0
    var tileHeight: Int = 0

    var nextObjectId: Int = 0
    var version: String = ""
    var mapName: String = ""

    val tileSets: MutableList<TileSet> = mutableListOf()
    val mapLayers: MutableList<Layer> = mutableListOf()
    val gidList: MutableList<Gid> = mutableListOf()

    init {
        val factory: DocumentBuilderFactory = DocumentBuilderFactory.newInstance()
        //factory.isIgnoringElementContentWhitespace = true
        val builder: DocumentBuilder = factory.newDocumentBuilder()

        try {
            val inputSource =
                InputSource(
                    StringReader(
                        ResourceManager.getResource<Resource.TiledResource>(
                            "/mapas/$fileName",
                            ResourceType.TILED
                        ).data.lines().joinToString("")
                    )
                )
            val document = builder.parse(inputSource)

            val mapNode = document.documentElement

            if (mapNode.nodeName != "map") {
                throw RuntimeException("Not a valid tmx map.")
            }

            mapName = fileName
            version = mapNode.getAttribute("version")
            mapWith = mapNode.getAttribute("width").toInt()
            mapHeight = mapNode.getAttribute("height").toInt()
            tileWidth = mapNode.getAttribute("tilewidth").toInt()
            tileHeight = mapNode.getAttribute("tileheight").toInt()
            nextObjectId = mapNode.getAttribute("nextobjectid").toInt()

            var lastGid = 0
            var tileCount = 0

            val tileSetsNode = mapNode.getElementsByTagName("tileset")

            for (i in 0 until tileSetsNode.length) {
                val ts = tileSetsNode.item(i)
                val tsi = (0 until ts.childNodes.length).map { ts.childNodes.item(it) } .firstOrNull { it.nodeName == "image" } ?: continue

                val tileSetImage = TileSetImage(
                    tsi.attributes.getNamedItem("width").nodeValue.toInt(),
                    tsi.attributes.getNamedItem("height").nodeValue.toInt(),
                    tsi.attributes.getNamedItem("source").nodeValue.substringAfterLast("/"),
                    tsi.attributes.getNamedItem("source").nodeValue
                        .substringAfterLast("/")
                        .substringBefore(".")
                        .toUInt()
                )

                val tileSet = TileSet(
                    firstGid = ts.attributes.getNamedItem("firstgid").nodeValue.toInt(),
                    name = ts.attributes.getNamedItem("name").nodeValue,
                    tileSetImage = tileSetImage,
                    tileWidth = ts.attributes.getNamedItem("tilewidth").nodeValue.toInt(),
                    tileHeight = ts.attributes.getNamedItem("tileheight").nodeValue.toInt(),
                    tileCount = ts.attributes.getNamedItem("tilecount").nodeValue.toInt(),
                    columns = ts.attributes.getNamedItem("columns").nodeValue.toInt(),
                    tileAmountWidth = (tileSetImage.imageWidth / ts.attributes.getNamedItem("tilewidth").nodeValue.toInt()),
                    lastGid = ((tileSetImage.imageWidth / ts.attributes.getNamedItem("tilewidth").nodeValue.toInt()) * (tileSetImage.imageHeight / ts.attributes.getNamedItem(
                        "tileheight"
                    ).nodeValue.toInt()) + ts.attributes.getNamedItem("firstgid").nodeValue.toInt()) - 1
                )

                if (lastGid < tileSet.lastGid)
                    lastGid = tileSet.lastGid

                tileSets.add(tileSet)

            }

            val layerNode = mapNode.getElementsByTagName("layer")

            for (i in 0 until layerNode.length) {
                val l = layerNode.item(i)

                val layerW = l.attributes.getNamedItem("width").nodeValue.toInt()
                val layerH = l.attributes.getNamedItem("height").nodeValue.toInt()

                val layer = Layer(
                    layerName = l.attributes.getNamedItem("name").nodeValue,
                    layerWidth = layerW,
                    layerHeight = layerH,
                    layerData = mutableListOf(),
                    tileCoordinates = Array(layerW) { Array(layerH) { 0 } }
                )

                val dataNode = (0 until l.childNodes.length).map { l.childNodes.item(it) }
                    .firstOrNull { it.nodeName == "data" } ?: continue

                val encoding = dataNode.attributes.getNamedItem("encoding").nodeValue
                var tempData = dataNode.textContent.trim()

                if (encoding == "base64") {
                    tempData = Base64.getDecoder().decode(tempData).toString(Charsets.ISO_8859_1)

                    val expectedSize = layerW * layerH * 4
                    val byteData = tempData.toByteArray(Charsets.ISO_8859_1)

                    for (i in 0 until expectedSize step 4) {
                        val id = byteData[i].toUInt() or
                                (byteData[i + 1].toUInt() shl 8) or
                                (byteData[i + 2].toUInt() shl 16) or
                                (byteData[i + 3].toUInt() shl 24)
                        layer.layerData.add(id.toUInt())
                    }

                    for (x in 0 until layerW) {
                        for (y in 0 until layerH) {
                            val tileId = layer.layerData[x + y * layerW].toInt()
                            layer.tileCoordinates[x][y] = tileId
                        }
                    }

                    mapLayers.add(layer)
                } else {
                    println(tempData)
                }
            }

            for (tileSet in tileSets) {
                val tsFirstGid = tileSet.firstGid
                val tsLastGid = tileSet.lastGid
                val tsColumns = tileSet.columns
                val tsTileWidth = tileSet.tileWidth
                val tsTileHeight = tileSet.tileHeight

                for (i in tsFirstGid..tsLastGid) {
                    val tile = i - (tsFirstGid - 1)
                    val sY = ceil((tile / tsColumns - 1).toFloat())
                    val sX = tile - (sY * tsColumns) - 1

                    gidList.add(i - 1, Gid(i, sX.toInt() * tsTileWidth, sY.toInt() * tsTileHeight))
                }
            }
        } catch (e: Exception) {
            println("Error loading map ${e.message}")
        }
    }

    fun getImageByGid(gid: Int): TileSet? {
        for (ts in tileSets) {
            if (gid >= ts.firstGid && gid <= ts.lastGid)
                return ts
        }

        return null
    }
}