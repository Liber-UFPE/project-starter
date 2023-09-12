package br.ufpe.liber.views

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe

class LinksHelperTest : BehaviorSpec({
    given("LinksHelper") {
        `when`(".Liber.link") {
            then("should create a link that is a path of Liber website") {
                LinksHelper.Liber.link("some-path") shouldBe "http://www.liber.ufpe.br/some-path"
            }

            then("should return Liber website URL when path is empty") {
                LinksHelper.Liber.link("") shouldBe "http://www.liber.ufpe.br/"
            }
        }
    }
})
