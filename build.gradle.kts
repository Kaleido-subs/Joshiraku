import myaa.subkt.ass.*
import myaa.subkt.tasks.*
import myaa.subkt.tasks.Mux.*
import myaa.subkt.tasks.Nyaa.*
import java.awt.Color
import java.time.*

plugins {
    id("myaa.subkt")
}

fun String.isKaraTemplate(): Boolean {
    return this.startsWith("code") || this.startsWith("template") || this.startsWith("mixin")
}

fun EventLine.isKaraTemplate(): Boolean {
    return this.comment && this.effect.isKaraTemplate()
}

subs {
    readProperties("sub.properties")
    episodes(getList("episodes"))


    val op_ktemplate by task<Automation> {
        if (propertyExists("OP")) {
            from(get("OP"))
        }

        video(get("premux"))
        script("0x.KaraTemplater.moon")
        macro("0x539's Templater")
        loglevel(Automation.LogLevel.WARNING)
    }


    val ed_ktemplate by task<Automation> {
        if (propertyExists("ED")) {
            from(get("ED"))
        }

        video(get("premux"))
        script("0x.KaraTemplater.moon")
        macro("0x539's Templater")
        loglevel(Automation.LogLevel.WARNING)
    }

    merge {
        from(get("dialogue"))

        if (propertyExists("OP")) {
            from(op_ktemplate.item()) {
                syncSourceLine("sync")
                syncTargetLine("opsync")
            }
        }

        if (propertyExists("ED")) {
            from(ed_ktemplate.item()) {
                syncSourceLine("sync")
                syncTargetLine("edsync")
            }
        }

        fromIfPresent(get("INS"), ignoreMissingFiles = true)
        fromIfPresent(getList("TS"), ignoreMissingFiles = true)

        includeExtraData(false)
        includeProjectGarbage(false)

        scriptInfo {
            title = get("group").get()
            scaledBorderAndShadow = true
        }
    }

    val cleanmerge by task<ASS> {
        from(merge.item())
        ass {
            events.lines.removeIf {
                it.isKaraTemplate()
                }
        }
    }

    val alt_track by task<ASS> {
        from(get("dialogue_alt"))

        includeExtraData(false)
        includeProjectGarbage(false)

        scriptInfo {
            title = get("group_alt").get()
            scaledBorderAndShadow = true
        }
    }

    chapters {
        from(cleanmerge.item())
        chapterMarker("chapter")
    }

    mux {
        title(get("title"))

        from(get("premux")) {
            video {
                lang("jpn")
                default(true)
            }
            audio {
                lang("jpn")
                default(true)
            }
            includeChapters(false)
            attachments { include(false) }
        }

        from(cleanmerge.item()) {
            tracks {
                lang("eng")
                name(get("group"))
                default(true)
                forced(false)
                compression(CompressionType.ZLIB)
            }
        }

        // Alt gg track
        from(alt_track.item()) {
            tracks {
                lang("eng")
                name(get("group_alt"))
                default(false)
                forced(false)
                compression(CompressionType.ZLIB)
            }
        }

        chapters(chapters.item()) { lang("eng") }

        skipUnusedFonts(true)

        attach(get("common_fonts")) {
            includeExtensions("ttf", "otf")
        }

        attach(get("fonts")) {
            includeExtensions("ttf", "otf")
        }

        if (propertyExists("OP")) {
            attach(get("opfonts")) {
                includeExtensions("ttf", "otf")
            }
        }

        if (propertyExists("ED")) {
            attach(get("edfonts")) {
                includeExtensions("ttf", "otf")
            }
        }

        out(get("muxout"))
    }

    tasks(getList("ncs")) {
        // will create mux.ncop1, mux.ncop2, etc tasks
        val run_ktemplate by task<Automation> {
            from(get("ncsubs"))

            video(get("ncpremux"))
            script("0x.KaraTemplater.moon")
            macro("0x539's Templater")
            loglevel(Automation.LogLevel.WARNING)
        }

        merge {
            from(run_ktemplate.item())

            includeExtraData(false)
            includeProjectGarbage(false)

            scriptInfo {
                title = "Kaleido-subs"
                scaledBorderAndShadow = true
            }
        }

        val cleanmerge by task<ASS> {
            from(merge.item())
            ass {
                events.lines.removeIf { it.isKaraTemplate() }
            }
        }

        // chapters {
        //     from(cleanmerge.item())
        //     chapterMarker("chapter")
        // }

        mux {
            title(get("title"))

            from(get("ncpremux")) {
                video {
                    lang("jpn")
                    default(true)
                }
                audio {
                    lang("jpn")
                    default(true)
                }
                includeChapters(false)
                attachments { include(false) }
            }

            from(cleanmerge.item()) {
                tracks {
                    lang("eng")
                    name(get("group"))
                    default(true)
                    forced(false)
                    compression(CompressionType.ZLIB)
                }
            }

            // chapters(chapters.item()) { lang("eng") }

            skipUnusedFonts(true)

            attach(get("common_fonts")) {
                includeExtensions("ttf", "otf")
            }

            attach(get("fonts")) {
                includeExtensions("ttf", "otf")
            }

            out(get("muxout"))
        }
    }
}
