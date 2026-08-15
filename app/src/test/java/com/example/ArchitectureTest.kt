package com.example

import com.tngtech.archunit.core.domain.JavaClasses
import com.tngtech.archunit.core.importer.ClassFileImporter
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes
import org.junit.BeforeClass
import org.junit.Test

class ArchitectureTest {

    companion object {
        private lateinit var importedClasses: JavaClasses

        @JvmStatic
        @BeforeClass
        fun setup() {
            // Сканируем проект ОДИН раз перед запуском ВСЕХ тестов в этом классе
            importedClasses = ClassFileImporter().importPackages("com.example.sound")
        }
    }

    @Test
    fun `components inside editSongInformation should only be accessed by that feature`() {

        // 1. Описываем строгое архитектурное правило
        val rule = classes()
            .that().resideInAPackage("..Presentation.editSongInformation.components..")
            .should().onlyBeAccessed().byAnyPackage(
                "..Presentation.editSongInformation..",
                "..Presentation.editSongInformation.components.."
            )

        // 3. Запускаем проверку
        rule.check(importedClasses)
    }
}