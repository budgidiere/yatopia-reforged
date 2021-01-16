package task

import ensureSuccess
import gitCmd
import org.apache.tools.ant.util.FileUtils
import org.gradle.api.Project
import org.gradle.api.Task
import rootProjectDir
import taskGroup
import toothpick
import upstreamDir
import upstreams
import Upstream
import java.nio.file.Files
import java.nio.file.Path


internal fun Project.createUpdateUpstreamTask(
    receiver: Task.() -> Unit = {}
): Task = tasks.create("updateUpstream") {
    receiver(this)
    group = taskGroup
    doLast {
        for (upstream in upstreams) {
            val serverRepoPatches = upstream.getRepoServerPatches()
            val apiRepoPatches = upstream.getRepoAPIPatches()
            val serverPatches = upstream.serverList
            val apiPatches = upstream.apiList
            val fileUtils = FileUtils.getFileUtils()
            if (upstream.useBlackList) {
                if (serverRepoPatches != null) {
                    var i = 0
                    for (patch in serverRepoPatches) {
                        i++
                        if (serverPatches != null && serverPatches.contains(patch)) {
                            continue
                        } else {
                            updatePatch(fileUtils, upstream, serverRepoPatches, patch, i, "server")
                        }
                    }
                }
                if (apiRepoPatches != null) {
                    var i = 0
                    for (patch in apiRepoPatches) {
                        i++
                        if (apiPatches != null && apiPatches.contains(patch)) {
                            continue
                        } else {
                            updatePatch(fileUtils, upstream, apiRepoPatches, patch, i, "api")
                        }
                    }
                }
            } else {
                if (serverPatches != null) {
                    var i = 0
                    for (patch in serverPatches) {
                        i++
                        if (serverRepoPatches != null && !serverRepoPatches.contains(patch)) {
                            continue
                        } else if (serverRepoPatches != null) {
                            updatePatch(fileUtils, upstream, serverRepoPatches, patch, i, "server")
                        }
                    }
                }
                if (apiPatches != null) {
                    var i = 0
                    for (patch in apiPatches) {
                        i++
                        if (apiRepoPatches != null && !apiRepoPatches.contains(patch)) {
                            continue
                        } else if (apiRepoPatches != null) {
                            updatePatch(fileUtils, upstream, apiRepoPatches, patch, i, "api")
                        }
                    }
                }
            }
        }
        ensureSuccess(gitCmd("fetch", dir = upstreamDir, printOut = true))
        ensureSuccess(gitCmd("reset", "--hard", toothpick.upstreamBranch, dir = upstreamDir, printOut = true))
        ensureSuccess(gitCmd("add", toothpick.upstream, dir = rootProjectDir, printOut = true))
        ensureSuccess(gitCmd("submodule", "update", "--init", "--recursive", dir = upstreamDir, printOut = true))
    }
}

private fun updatePatch(
    fileUtils: FileUtils,
    upstream: Upstream,
    serverRepoPatches: MutableList<String>,
    patch: String,
    i: Int,
    folder: String
) {
    val folderFile = Path.of("${upstream.patchPath}/$folder").toFile()
    val folderList = folderFile.listFiles()
    val nullFolder = folderList == null
    if (nullFolder || folderList.isEmpty()) {
        needToPop = true
        folderFile.mkdirs()
    }
    if (upstream.getCurrentCommitHash() != upstream.uptreamCommit || needToPop || nullFolder || folderList.isEmpty()) {
        System.out.println("test")
        System.out.println("${upstream.repoPath}/patches/$folder/" +
                "${String.format("%04d", serverRepoPatches.indexOf(patch) + 1)}-$patch")
        System.out.println("${upstream.patchPath}/$folder/${String.format("%04d", i)}-$patch")
        fileUtils.copyFile("${upstream.repoPath}/patches/$folder/" +
                "${String.format("%04d", serverRepoPatches.indexOf(patch) + 1)}-$patch",
            "${upstream.patchPath}/$folder/${String.format("%04d", i)}-$patch"
        )
    }
}

var needToPop = false;