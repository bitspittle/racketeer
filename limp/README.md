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
evaluating `2 * 5 + 1` depends on knowledge of operator precedence, where there it evaluates to 11 unless you
explicitly add parentheses (as in `2 * (5 + 1)`).

### Deferred expressions

By default, when you evaluate a Limp expression, every part of the expression is evaluated immediately. However, by
prefixing parts of the expression with an apostrophe (`'`), it tells the evaluator to postpone evaluation on that for
later.

A concrete example is the "set variable" method, which looks like this: `set '$example 123`. If you didn't defer the
variable name there, instead writing `set $example 123`, the evaluator would try to evaluate the variable immediately
and barf because it hasn't been defined yet!

Another example for deferment is for lambdas. Here's filter: `filter $numbers '(>= $it 0)`.
This expression means "take in a list of numbers and return a new list which only has positive numbers in it". We don't
want to run the logic for checking if a number is positive immediately! Instead, we want to let the `filter` method call
it internally. (It is also up to the `filter` method to correctly define the `$it` variable for us.)

## Using Limp in your project

Limp works by combining two classes, `Environment` and `Evaluator`.

### Environment

An environment is a scoped collection of methods, variables, and converters.

When constructed, a new environment is totally empty, but you can use the provided utility method,
`Environment.installDefaults()`, to add a bunch of useful logic, math, and other generally helpful behavior. (You
probably want to do this!)

#### Method

You can define and add methods.

A method is some logic, tagged with a name and specifying the number of arguments it can consume.

For example, the add method takes two arguments (and returns their sum, if the two arguments are integers).

A method can additionally be configured to accept optional parameters as well as to consume all remaining arguments in
the expression. The latter is useful for a method that can take a dynamic number of arguments, say a method like
`(list 1 2 3 4 5 6)`.

#### Variable

A variable is a value tagged with a name.

You can register these directly into the environment using `env.storeValue("\$example", 123)`. Or, if you added the
`SetMethod` into the environment, a user can define a variable using syntax like `set '$example 123`

By convention, when you define a variable, you should prepend it with a `$`, for readability. However, it's not strictly
required you do so.

#### Converter

A converter is some logic to automatically convert a value of one type into another, at runtime. This can be useful to
make some of your methods a bit more flexible.

For example, if you have a method that takes in a list, but it gets passed a single item, you can create a converter
that converts non-list items into a singleton list for you on the fly.

It can also be a good way to handle default values, by adding a converter for your method that converts a placeholder
value into a default value for you.

You should be careful with abusing converters, however, as if there are too many, an unexpected conversion might happen
behind your back.

#### Scope

By scoped, what it means is you can use `pushScope` (and `popScope`) at any point to introduce (and later remove) a new
local scope. All methods, variables, and converters defined while this scope is active will be discarded when it is
removed.

### Evaluator

An evaluator is a class which is responsible for taking a code statement and an accompanying environment and processing
the two together to produce a result.

Most of the time you can just instance an evaluator anywhere you have an environment and fire its `evaluate` method:

```kotlin
val env: Environment = ...
val evaluator = Evaluator()
evaluator.evaluate(env, "+ 1 2")
```

This will both parse the code AND run through the processed result, pulling values out of and writing others back into
the environment as it goes along. If it dies at any point, it will throw an `EvaluationException` explaining what went
wrong.

#### Parsing Expressions

Although evaluators handle parsing for you, it's trivial to parse a limp expression on your own. Just use the
`Expr.parse` method:

```kotlin
val compiled = Expr.parse("+ 1 2")
```

You can feed in such compiled results into an evaluator, e.g. `evaluator.evaluate(env, compiled)`, instead of raw code.

This change is unlikely to matter too much in most applications -- if performance is *that* critical for you, you
probably should look elsewhere -- but it can be useful to compile all your code upfront, so that an accidental syntax
error will be caught at startup time instead of hours later at runtime.

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

// If we get a Placeholder object, translate it into some other value, e.g. _ -> 0 or "hi"
class PlaceholderConverter<T: Any>(private val toValue: T) : Converter<T>(toValue::class) {
    override fun convert(value: Any): T? {
        return if (value === Placeholder) { toValue } else { null }
    }
}

val env = Environment()
// Pretend there wasn't already an add method...
env.add(object : Method("add", 2) {
   override suspend fun invoke(env: Environment, params: List<Value>, optionals: Map<String, Value>, rest: List<Value>): Value {
       // This code supports: "add 1 2", "add _ 2", "add 1 _", and "add _ _", thanks to our converter
       val sum = env.scoped {
           env.add(PlaceholderConverter(0))
           val a = env.expectConvert<Int>(params[0])
           val b = env.expectConvert<Int>(params[1])
           a + b
       }

       return sum
   }
})
```
