package importer

import java.nio.file.Path
import kotlin.io.path.copyTo
import kotlin.test.assertNotNull

fun copyResourceToDirectory(resourceName: String, directory: Path): Path {
    val resource = Path.of(
        assertNotNull(
            object {}.javaClass.getResource(resourceName),
            "Resource $resourceName doesn't exist"
        ).toURI()
    )
    return resource.copyTo(directory.resolve(resource.fileName))
}

