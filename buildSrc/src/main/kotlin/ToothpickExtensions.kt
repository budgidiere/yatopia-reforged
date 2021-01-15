import org.gradle.api.Project
import org.gradle.kotlin.dsl.findByType
import java.io.File
import java.io.FileInputStream
import java.lang.Boolean
import java.nio.file.Path
import java.util.*
import kotlin.collections.ArrayList

val Project.toothpick: ToothpickExtension
    get() = rootProject.extensions.findByType(ToothpickExtension::class)!!

fun Project.toothpick(receiver: ToothpickExtension.() -> Unit) {
    toothpick.project = this
    receiver(toothpick)
    allprojects {
        group = toothpick.groupId
        version = "${toothpick.minecraftVersion}-${toothpick.nmsRevision}"
    }
    configureSubprojects()
    initToothpickTasks()
}

fun getUpstreams(rootProjectDir: File): ArrayList<Uptream> {
    val configDir = rootProjectDir.resolve("$rootProjectDir/upstreamConfig")
    val upstreams = configDir.listFiles()
    val uptreamArray = ArrayList<Uptream>()
    val prop = Properties()
    for (upstream in upstreams) {
        prop.load(FileInputStream(upstream))
        uptreamArray.add(Uptream(prop.getProperty("name"),
            Boolean.parseBoolean(prop.getProperty("useBlackList")),
            Arrays.asList(prop.getProperty("list").split(",".toRegex()).toTypedArray()) as ArrayList<String>,
            rootProjectDir))
    }
    return uptreamArray;
}

val Project.lastUpstream: File
    get() = rootProject.projectDir.resolve("last-${toothpick.upstreamLowercase}")

val Project.rootProjectDir: File
    get() = rootProject.projectDir

val Project.upstreamDir: File
    get() = rootProject.projectDir.resolve(toothpick.upstream)

val Project.projectPath: Path
    get() = projectDir.toPath()

val Project.upstreams: ArrayList<Uptream>
    get() = getUpstreams(rootProject.projectDir)

val Project.forkName: String
    get() = toothpick.forkName
