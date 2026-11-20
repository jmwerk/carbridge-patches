package org.carbridge.patches.androidauto

import app.morphe.patcher.patch.PatchException
import javax.xml.parsers.DocumentBuilderFactory
import org.carbridge.core.CarOptions
import org.carbridge.core.CarPatch
import org.carbridge.core.ManifestElement
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.w3c.dom.Document
import org.w3c.dom.Element

/** Runs the shared Android Auto rules through the bundle's own DOM adapter. */
class DomHostTest {

    private val written = mutableMapOf<String, String>()

    @Test
    fun patchesLauncherActivity() {
        val (doc, host) = host(MANIFEST)
        CarPatch.apply(host)

        val main = activity(host, ".Main")
        val categories = main.children("intent-filter").single().children("category").map { it.getString(CarPatch.NAME) }
        assertTrue(categories.containsAll(CarPatch.CAR_CATEGORIES))
        assertEquals("true", main.getString(CarPatch.RESIZEABLE))
        assertEquals("game", host.application.getString(CarPatch.APP_CATEGORY))
        assertEquals(
            "@xml/${CarPatch.DESCRIPTOR_NAME}",
            host.application.children("meta-data").single { it.getString(CarPatch.NAME) == CarPatch.CAR_APP_META }
                .getString(CarPatch.RESOURCE),
        )

        // Permissions must come before <application> for the manifest to compile
        val tags = elements(doc.documentElement).map { it.tagName + ":" + it.getAttribute("android:name") }
        for (perm in CarPatch.CAR_PERMISSIONS) {
            assertTrue(perm, tags.indexOf("uses-permission:$perm") in 0 until tags.indexOf("application:"))
        }
    }

    @Test
    fun writesAutomotiveDescriptor() {
        val (_, host) = host(MANIFEST)
        CarPatch.apply(host)

        val xml = written.getValue("res/xml/${CarPatch.DESCRIPTOR_NAME}.xml")
        val uses = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(xml.byteInputStream())
            .documentElement.also { assertEquals("automotiveApp", it.tagName) }
            .getElementsByTagName("uses")
        assertEquals(1, uses.length)
        assertTrue((uses.item(0) as Element).getAttribute("name").isNotEmpty())
    }

    @Test
    fun readsSymbolicAndNumericOrientation() {
        val (_, host) = host(MANIFEST)
        assertEquals(1, activity(host, ".Main").getInt(CarPatch.SCREEN_ORIENTATION))
        assertEquals(7, activity(host, ".Numeric").getInt(CarPatch.SCREEN_ORIENTATION))

        CarPatch.apply(host, CarOptions(fillScreen = true))
        assertNull(activity(host, ".Main").getString(CarPatch.SCREEN_ORIENTATION))
        assertNull(activity(host, ".Numeric").getString(CarPatch.SCREEN_ORIENTATION))
    }

    @Test
    fun keepMediaServicesOption() {
        val (_, stripped) = host(MANIFEST)
        CarPatch.apply(stripped)
        assertTrue(service(stripped, ".Media").children("intent-filter").isEmpty())

        val (_, kept) = host(MANIFEST)
        CarPatch.apply(kept, CarOptions(keepMediaServices = true))
        assertEquals(1, service(kept, ".Media").children("intent-filter").size)
    }

    @Test(expected = PatchException::class)
    fun failsWithoutApplication() {
        host("""<manifest xmlns:android="http://schemas.android.com/apk/res/android" package="a.b"/>""")
    }

    private fun host(xml: String): Pair<Document, DomHost> {
        val doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(xml.byteInputStream())
        return doc to DomHost(doc) { path, text -> written[path] = text }
    }

    private fun activity(host: DomHost, name: String): ManifestElement =
        host.application.children("activity").single { it.getString(CarPatch.NAME) == name }

    private fun service(host: DomHost, name: String): ManifestElement =
        host.application.children("service").single { it.getString(CarPatch.NAME) == name }

    private fun elements(parent: Element): List<Element> {
        val nodes = parent.childNodes
        return (0 until nodes.length).mapNotNull { nodes.item(it) as? Element }
    }

    private companion object {
        // Decoded the way Morphe hands it to patches: attributes as android:<name> text
        val MANIFEST = """
            <manifest xmlns:android="http://schemas.android.com/apk/res/android" package="a.b">
              <uses-permission android:name="android.permission.INTERNET"/>
              <application>
                <activity android:name=".Main" android:screenOrientation="portrait">
                  <intent-filter>
                    <action android:name="android.intent.action.MAIN"/>
                    <category android:name="android.intent.category.LAUNCHER"/>
                  </intent-filter>
                </activity>
                <activity android:name=".Numeric" android:screenOrientation="7"/>
                <service android:name=".Media">
                  <intent-filter><action android:name="android.media.browse.MediaBrowserService"/></intent-filter>
                </service>
              </application>
            </manifest>
        """.trimIndent()
    }
}
