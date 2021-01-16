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
                            fileUtils.copyFile("${upstream.repoPath}/patches/server/${String.format("%04d", 
                                    serverRepoPatches.indexOf(patch) + 1 )}-$patch",
                                "${upstream.patchPath}/server/${String.format("%04d", i)}-$patch")
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
                            fileUtils.copyFile("${upstream.repoPath}/patches/api/${String.format("%04d",
                                apiRepoPatches.indexOf(patch) + 1 )}-$patch",
                                "${upstream.patchPath}/api/${String.format("%04d", i)}-$patch")
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
                            fileUtils.copyFile("${upstream.repoPath}/patches/server/${String.format("%04d",
                                serverRepoPatches.indexOf(patch) + 1 )}-$patch",
                                "${upstream.patchPath}/server/${String.format("%04d", i)}-$patch")
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
                            fileUtils.copyFile("${upstream.repoPath}/patches/api/${String.format("%04d",
                                apiRepoPatches.indexOf(patch) + 1 )}-$patch",
                                "${upstream.patchPath}/api/${String.format("%04d", i)}-$patch")
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
