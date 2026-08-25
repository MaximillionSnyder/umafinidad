# kotlinx.serialization (reglas oficiales adaptadas al paquete del app)
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt

-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}

-keep,includedescriptorclasses class com.maximillionsnyder.umafinidad.**$$serializer { *; }
-keepclassmembers class com.maximillionsnyder.umafinidad.** {
    *** Companion;
}
-keepclasseswithmembers class com.maximillionsnyder.umafinidad.** {
    kotlinx.serialization.KSerializer serializer(...);
}
