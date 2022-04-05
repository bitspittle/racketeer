# limp 

This project implements a minimal, toy Lisp-inspired language that works in a Kotlin multiplatform context.

## Expressions

Limp is designed to be very simple, with a limited number of expression types:

- Quoted strings (e.g. `"Hello"`)
- Integers (e.g. `123` or `-456`)
- Symbols (e.g. `some-identifier-name` or `+`)
- Blocks (e.g. `(+ 1 2)`)
- Deferred expressions (e.g. `'symbol` or `'(+ 1 2)`)

You can also use the `#` symbol to indicate a comment that runs to the end of the line.

### Polish Notation

For parsing simplicity, Limp uses polish (also called *prefix*) notation. That is, addition looks like `(+ 1 2)` instead
of `(1 + 2)`.

This removes ambiguity if parentheses aren't used. That is, `(* 2 + 5 1)` is always 12, while in a language like Java,
evaluating `2 * 5 + 1` depends on knowledge of operator precedence, and it actually evaluates to 11 unless you
explicitly add parentheses (as in `2 * (5 + 1)`).

### Deferred expressions

By default, when you evaluate a Limp expression, ever part of the expression is evaluated immediately. However, by
preceding parts of the expression with an apostrophe, it tells the evaluator to postpone evaluation on that for later.

This can be very useful when defining logic that you don't want to execute yourself, but you want to hand down into some
method and have it do it later.

One example is the "set variable" method, which can look like this: `set '$example 123`. If you didn't defer the
variable name there, instead writing `set $example 123`, the evaluator would try to evaluate the variable immediately
and barf because it hasn't been defined yet!

Another example for deferment is when using some list iterating method, like a filter: `filter $numbers '(>= $it 0)`.
This expression means take in a list of numbers and return a new list which only has positive numbers in it. You can see
that we define some logic (checking if a value is positive) but we don't want to run it ourselves! We want to let the
`filter` method call it internally. (It is also up to the `filter` method to correctly define the `$it` variable for
us.)

## Using Limp in your project

Limp works by combining two classes, `Environment` and `Evaluator`.

### Environment

An environment is a scoped collection of methods, variables, and converters.

When constructed, a new environment is totally empty, but you can use the provided utility method,
`Environment.installUsefulDefaults()`, to add a bunch of useful logic, math, and other generally helpful behavior. 

#### Method

A method is some logic, tagged with a name and specifying the number of arguments it can consume.

A method can additionally be configured to accept optional parameters as well as to consume all remaining arguments in
the expression. The latter is useful for a method that can take a dynamic number of arguments, say a method like
`(list 1 2 3 4 5 6)`.

#### Variable

A variable is a value tagged with a name.

You can register these directly into the environment or, if you installed the `SetMethod` into the environment, a user
can define a variable using syntax like `set '$example 123`

By convention, when you define a variable, you should prepend it with a `$`, for readability. However, it's not strictly
required you do so.

#### Converter

A converter is some logic to automatically convert a value of one type into another, at runtime. This can be useful to
make some of your methods a bit more flexible.

For example, if you have a method that takes in a list, but it gets passed a single item, you can create a converter
that converts it into a singleton list for you on the fly.

It can also be a good way to handle default values, by adding a converter for your method that converts a placeholder
value into a default value for you.

You should be careful with abusing converters, however, as if there are too many, an unexpected conversion might happen
behind your back.

#### Scope

By scoped, what it means is you can use `pushScope` (and `popScope`) at any point to introduce (and later remove) a new
local scope. All methods, variables, and converters defined while this scope is active will be discarded when it is
removed.

### Code Examples

```kotlin
// The basics
val env = Environment()
env.installDefaults()

val evaluator = Evaluator()
val result = evaluator.evaluate(env, "* 2 + 5 1")
assertThat(result).isEqualTo(12)
```

```kotlin
// Defining a method
val env = Environment()
env.add(object : Method("concat", 2) {
   override suspend fun invoke(env: Environment, params: List<Value>, rest: List<Value>): Value {
       return env.expectConvert<String>(params[0]) + env.expectConvert<String>(params[1])
   }
})

val evaluator = Evaluator()
val result = evaluator.evaluate(env, "concat \"Hello \" \"World\"")
assertThat(result).isEqualTo("Hello World")
```

```kotlin
// Using a converter

// This is actually defined in Limp, but we pretend here as if we wrote it from scratch.
// A converter tries to convert one value type to another. Here, we check if that value is a special
// Placeholder instance, and if so, convert to some default value specified in the constructor.
class PlaceholderConverter<T: Any>(private val toValue: T) : Converter<T>(toValue::class) {
    override fun convert(value: Any): T? {
        return if (value === Placeholder) { toValue } else { null }
    }
}

val env = Environment()
// "take" pulls item from a list. The first argument is the list and the second argument is a count.
// The special placeholder character means "take everything"
// > take $list 2 -> new list of size 2
// > take $list _ -> take the whole list
env.add(object : Method("take", 2) {
   override suspend fun invoke(env: Environment, params: List<Value>, optionals: Map<String, Value>, rest: List<Value>): Value {
       val listIn = env.expectConvert<List>(params[0])
       val count = env.scoped {
           env.add(PlaceholderConverter(listIn.size))
           env.expectConvert<Int>(params[1])
       }

       return listIn.take(count)
   }
})
env.set("$numbers", listOf(1, 2, 3, 4, 5))

val evaluator = Evaluator()
run {
    val result = evaluator.evaluate(env, "take $numbers 3")
    assertThat(result).isEqualTo(listOf(1, 2, 3))
}
run {
    val result = evaluator.evaluate(env, "take $numbers _")
    assertThat(result).isEqualTo(listOf(1, 2, 3, 4, 5))
}
```
