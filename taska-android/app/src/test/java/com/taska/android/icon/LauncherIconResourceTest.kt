package com.taska.android.icon

import java.io.File
import java.security.MessageDigest
import org.junit.Test
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue

class LauncherIconResourceTest {
    private val projectRoot = File(System.getProperty("user.dir"))

    @Test
    fun `launcher resources use the approved signal green in every variant`() {
        val expectedRasterHashes = mapOf(
        "app/src/dev/res/mipmap-hdpi/ic_launcher.webp" to "0051cdf53c79f0443d0a404edeee7ba9ce5ebe57a97c1a24468512281016d4e0",
        "app/src/dev/res/mipmap-hdpi/ic_launcher_round.webp" to "eeeb5f205b279537a7547790bd01ac8617c0a9a9c8705b47232661ee8b54a431",
        "app/src/dev/res/mipmap-mdpi/ic_launcher.webp" to "b92f5025db4198ac46ceb133447990bdbf299c2c016dd2496b264f29843d141a",
        "app/src/dev/res/mipmap-mdpi/ic_launcher_round.webp" to "66cef79f4e3d0bba1a21fc2869f9e0b917ff01fdb6bda73b09223e12a5c45258",
        "app/src/dev/res/mipmap-xhdpi/ic_launcher.webp" to "818eac0c615ff250816e79f2e764d56e8d918e20e919fe7d4bf6d060a62107f0",
        "app/src/dev/res/mipmap-xhdpi/ic_launcher_round.webp" to "95d6ef15419497075465d72b94fc997d921367efabe955e33ca5baf27f126bf2",
        "app/src/dev/res/mipmap-xxhdpi/ic_launcher.webp" to "f1e4efc9fa309399a6399544873e7ead89b402b06879b2b0d67b076044786bca",
        "app/src/dev/res/mipmap-xxhdpi/ic_launcher_round.webp" to "16405501da2c46d844b21be684ef731359008fc1a5b37f8b2c28e5b65e3f0a1a",
        "app/src/dev/res/mipmap-xxxhdpi/ic_launcher.webp" to "683ab7c8020a2c154cc8c5d23d583fc2dbfc72e7d69ffd25d50eaf8742397196",
        "app/src/dev/res/mipmap-xxxhdpi/ic_launcher_round.webp" to "e0b19402540344575c9857232353e7961b0af4b0ae5333f5d062f975e139137c",
        "app/src/main/res/mipmap-hdpi/ic_launcher.webp" to "479a0b38e4e0ccb19e2aa796f7377b02eb4c339fe2936b88f9399a8701490b69",
        "app/src/main/res/mipmap-hdpi/ic_launcher_round.webp" to "3608f380ac857aba236d0129a3887bfbd55c360e176ea90f152b1380bb6bf082",
        "app/src/main/res/mipmap-mdpi/ic_launcher.webp" to "ddc7122406850ff814807f9269612db63afaa36c737f4a6bc35fcf95ed8e4f0c",
        "app/src/main/res/mipmap-mdpi/ic_launcher_round.webp" to "685c0de604036c8da2113f7d2c5ad24107788d35b7def96a487c90b510f91424",
        "app/src/main/res/mipmap-xhdpi/ic_launcher.webp" to "f964595d31cafb3754e83e23e2cc50279c1c13c22419ff04fa78af6863045fb7",
        "app/src/main/res/mipmap-xhdpi/ic_launcher_round.webp" to "515e47fddc4f10bc722128d499a8265586a33f375d29037fcc44d3a5c5684fbd",
        "app/src/main/res/mipmap-xxhdpi/ic_launcher.webp" to "89dd219581b56a8eaf78fff054de2dc70ea8e7f547e9a50b0dab8d812a6f8845",
        "app/src/main/res/mipmap-xxhdpi/ic_launcher_round.webp" to "2bfe9c5422d59b56cca22999db5f4fc30fa6f83d51a6db9f60f564c2d16cdcda",
        "app/src/main/res/mipmap-xxxhdpi/ic_launcher.webp" to "1a474a3eb9400d75770e07ed29e579865e20d317226853c3774070bf2b8c1348",
        "app/src/main/res/mipmap-xxxhdpi/ic_launcher_round.webp" to "9c99b29deab1ef20cb5822bf419d68fcafae5a391afe8fdfbfb6796b1bd5b286",
        )

        val actualRasterPaths = projectRoot.resolve("app/src").walkTopDown()
            .filter { it.isFile && it.extension == "webp" && it.name.startsWith("ic_launcher") }
            .map { it.relativeTo(projectRoot).invariantSeparatorsPath }
            .toSet()

        assertEquals(expectedRasterHashes.keys, actualRasterPaths)
        expectedRasterHashes.forEach { (relativePath, expectedHash) ->
            assertEquals(expectedHash, projectRoot.resolve(relativePath).sha256(), relativePath)
        }

        val background = projectRoot.resolve("app/src/main/res/drawable/ic_launcher_background.xml").readText()
        assertTrue(background.contains("#14B37D"))
        assertFalse(background.contains("#FF8A3D", ignoreCase = true))
    }

    @Test
    fun `adaptive and legacy resources preserve established forms and variant geometry`() {
        listOf("ic_launcher.xml", "ic_launcher_round.xml").forEach { name ->
            val adaptiveIcon = projectRoot.resolve("app/src/main/res/mipmap-anydpi-v26/$name").readText()
            assertTrue(adaptiveIcon.contains("@drawable/ic_launcher_background"), name)
            assertTrue(adaptiveIcon.contains("@drawable/ic_launcher_foreground"), name)
        }

        val mainForeground = projectRoot.resolve("app/src/main/res/drawable/ic_launcher_foreground.xml").readText()
        val devForeground = projectRoot.resolve("app/src/dev/res/drawable/ic_launcher_foreground.xml").readText()
        val checkmarkPath = "M40.5,56.25 L50.625,66.375 L67.5,45"
        assertTrue(mainForeground.contains(checkmarkPath))
        assertTrue(devForeground.contains(checkmarkPath))
        assertTrue(devForeground.contains("#FFD600"), "The development badge must remain identifiable")
    }

    private fun File.sha256(): String = MessageDigest.getInstance("SHA-256")
        .digest(readBytes())
        .joinToString("") { "%02x".format(it) }
}
