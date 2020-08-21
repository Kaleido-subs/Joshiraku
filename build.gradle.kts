
import myaa.subkt.tasks.*
import myaa.subkt.tasks.Mux.*
import myaa.subkt.tasks.Nyaa.*
import java.awt.Color
import java.time.*

plugins {
    id("myaa.subkt")
}

subs {
    readProperties("sub.properties")
    episodes(getList("episodes"))

    merge {
        from(get("dialogue")) {
            incrementLayer(10)
        }

        if (file(get("OP")).exists()) {
            from(get("OP"))
        }

        if (file(get("ED")).exists()) {
            from(get("ED"))
        }

        if (file(get("IS")).exists()) {
            from(get("IS"))
        }

        if (file(get("TS")).exists()) {
            from(get("TS"))
        }

        out(get("mergedname"))
    }

    chapters {
        from(get("chapters"))
        chapterMarker("chapter")
    }

    mux {
        from(get("premux")) {
            includeChapters(false)
			attachments { include(false) }
        }

		from(merge.item()) {
			tracks {
				lang("eng")
                name("Kaleido-subs")
				default(true)
				forced(false)
				compression(CompressionType.ZLIB)
			}
		}

        chapters(chapters.item()) { lang("eng") }

        attach(get("fonts")) {
            includeExtensions("ttf", "otf")
        }

        out(get("muxout"))
    }
}
