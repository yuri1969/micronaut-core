package io.micronaut.expressions

import io.micronaut.ast.transform.test.AbstractBeanDefinitionSpec
import io.micronaut.context.env.PropertySource
import io.micronaut.context.exceptions.NoSuchBeanException

class TestExpressionsUsageSpec extends AbstractBeanDefinitionSpec {

    void "test expression array in requires"() {
        given:
        def ctx = buildContext("""
            package test
            import io.micronaut.context.annotation.Requires
            import jakarta.inject.Singleton

            @Singleton
            @Requires(env = ["#{ 'test' }"])
            class Expr {
            }
        """)

        when:
        getBean(ctx, "test.Expr")

        then:
        noExceptionThrown()

        cleanup:
        ctx.close()
    }

    void "test requires expression property value"() {
        given:
        def ctx = buildContext("""
            package test
            import io.micronaut.context.annotation.Requires
            import jakarta.inject.Singleton

            @Singleton
            @Requires(property = 'test-property', value = "#{ 'test-value'.toUpperCase() }")
            class Expr {
            }
        """)

        def type = ctx.classLoader.loadClass('test.Expr')

        when:
        ctx.environment.addPropertySource(PropertySource.of("test", ['test-property': 'TEST-VALUE']))
        ctx.getBean(type)

        then:
        noExceptionThrown()

        cleanup:
        ctx.close()
    }

    void "test requires expression context value"() {
        given:
        def ctx = buildContext("""
            package test

            import io.micronaut.context.annotation.ConfigurationProperties
            import io.micronaut.context.annotation.EvaluatedExpressionContext
            import io.micronaut.context.annotation.Requires

            import jakarta.inject.Singleton

            @Singleton
            @Requires(property = 'test.enabled', value = "#{ #enabled }")
            class Expr {
            }

            @ConfigurationProperties('test')
            @EvaluatedExpressionContext
            class TestContext {
                boolean enabled
            }
        """)

        def type = ctx.classLoader.loadClass('test.Expr')

        when:
        ctx.environment.addPropertySource(PropertySource.of("test", ['test.enabled': false]))
        ctx.getBean(type)

        then:
        noExceptionThrown()

        cleanup:
        ctx.close()
    }

    void "test disabled by expression bean"() {
        given:
        def ctx = buildContext("""
            package test

            import io.micronaut.context.annotation.ConfigurationProperties
            import io.micronaut.context.annotation.EvaluatedExpressionContext
            import io.micronaut.context.annotation.Requires

            import jakarta.inject.Singleton

            @Singleton
            @Requires(property = 'test.property', value = "#{ 5 * 2 }")
            class Expr {
            }

            @ConfigurationProperties('test')
            @EvaluatedExpressionContext
            class TestContext {
                boolean enabled
            }
        """)

        def type = ctx.classLoader.loadClass('test.Expr')

        when:
        ctx.environment.addPropertySource(PropertySource.of("test", ['test.property': 15]))
        ctx.getBean(type)

        then:
        thrown(NoSuchBeanException)

        cleanup:
        ctx.close()
    }
}
