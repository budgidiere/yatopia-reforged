import org.gradle.api.Project
import java.io.File
import java.io.FileWriter
import java.nio.file.Files
import java.nio.file.Path
import java.util.*
import java.util.stream.Collectors
import kotlin.collections.ArrayList

open class Upstream(in_name: String, in_useBlackList: Boolean, in_list: ArrayList<String>?, in_rootProjectDir: File, in_project: Project) {
    var name: String = in_name
    var useBlackList: Boolean = in_useBlackList
    private var list: ArrayList<String>? = in_list
    private var rootProjectDir: File = in_rootProjectDir

    var serverList = in_list?.stream()?.filter { patch -> patch.startsWith("server/") }
        ?.map { patch -> patch.substring(7, patch.length) }?.collect(Collectors.toList())
    var apiList = in_list?.stream()?.filter { patch -> patch.startsWith("API/") }
        ?.map { patch -> patch.substring(4, patch.length) }?.collect(Collectors.toList())


    var patchPath = Path.of("$rootProjectDir/patches/$name/patches")
    var repoPath = Path.of("$rootProjectDir/upstream/$name")

    var project = in_project

    var uptreamCommit = getUpstreamCommitHash()

    private fun getUpstreamCommitHash(): String {
        val commitFileFoler = Path.of("$rootProjectDir/upstreamCommits")
        val commitFilePath = Path.of("$commitFileFoler/$name")
        val commitFile = commitFilePath.toFile()
        var commitHash: String
        if (commitFile.isFile) {
            commitHash = Files.readAllLines(commitFilePath).toString()
            commitHash = commitHash.substring(1, commitHash.length - 1)
            if (commitHash == "") {
                commitHash = updateHashFile(commitFile)
            }
        } else {
            Files.createFile(commitFilePath)
            commitHash = updateHashFile(commitFile)
        }
        return commitHash;
    }

    public fun updateUpstreamCommitHash() {
        uptreamCommit = getUpstreamCommitHash()
    }

    private fun updateHashFile(commitFile: File): String {
        var commitHash: String
        commitHash = project.getCommitHash()
        val fileWriter = FileWriter(commitFile)
        fileWriter.use { out -> out.write(commitHash) }
        fileWriter.close()
        return commitHash
    }

    private fun Project.getCommitHash(): String = gitHash(repo = repoPath.toFile())
}