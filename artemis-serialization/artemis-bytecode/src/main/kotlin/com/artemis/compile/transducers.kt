package com.artemis.compile

import com.badlogic.gdx.utils.Json
import com.badlogic.gdx.utils.JsonReader
import com.badlogic.gdx.utils.JsonValue
import net.onedaybeard.transducers.*
import java.lang.reflect.Field
import java.lang.reflect.Modifier.STATIC
import java.lang.reflect.Modifier.TRANSIENT
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.reflect.KClass

private val jsonReader = CoreJsonReader()

@Suppress("UNCHECKED_CAST")
fun <R : MutableList<A>, A, B> intoList(xf: Transducer<A, B>,
                                        input: Iterable<B>): R =
	transduce(xf, object : ReducingFunction<R, A> {
		override fun apply(result: R,
		                   input: A,
		                   reduced: AtomicBoolean): R {
			result.add(input)
			return result
		}
	}, (mutableListOf<A>() as R), input)

operator fun <A, B, C> Transducer<B, C>.plus(right: Transducer<A, in B>): Transducer<A, C>
	= this.comp(right)

val objectToType  = map { t: Any -> t.javaClass.kotlin }
val kotlinToJava  = map { t: KClass<*> -> t.java }
val classToFields = mapcat { t: Class<*> -> t.declaredFields.asIterable() }
val withParents = mapcat { t: Class<*> ->
	val types = mutableListOf(t)

	var current = t.superclass
	while (current != null) {
		types += current
		current = current.superclass
	}

	types
}

val validFields: Transducer<Field, Field> =
	filter { 0 == (it.modifiers and (STATIC or TRANSIENT)) }

val allFields: Transducer<Field, KClass<*>> =
	kotlinToJava + withParents + classToFields

fun toJson(json: Json): Transducer<String, Any>
	= map { json.prettyPrint(it) }


val toJsonValue: Transducer<JsonValue, String>
	= map { JsonReader().parse(it) }

fun asSymbolsOf(owner: KClass<*>) = map { f: Field -> Symbol(owner.java, f.name, f.type) }


fun symbolToNode(json: JsonValue) : Transducer<Node, Symbol> {
	return map { symbol: Symbol ->
		if (isBuiltInType(symbol)) {
			val payload = jsonReader.read(symbol.type, json[symbol.field])
			Node(symbol.type, symbol.field, payload)
		} else {
			toNode(symbol.type, json[symbol.field], symbol.field)
		}
	}
}
