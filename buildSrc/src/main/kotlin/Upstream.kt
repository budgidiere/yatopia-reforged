import java.io.File
import java.nio.file.Path

class Uptream(in_name: String, in_useBlackList: Boolean, in_list: ArrayList<String>, in_rootProjectDir: File) {
    var name: String = in_name
    var useBlackList: Boolean = in_useBlackList
    var list: ArrayList<String> = in_list
    private var rootProjectDir: File = in_rootProjectDir

    var patchPath = Path.of("$rootProjectDir/patches/$name")
    var repoPath = Path.of("$rootProjectDir/upstream/$name")
}