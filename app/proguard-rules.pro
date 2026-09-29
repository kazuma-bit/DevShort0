# DevToggle Proguard / R8 Rules

# Keep Compose preview & runtime annotations
-keepattributes *Annotation*,InnerClasses,EnclosingMethod

# Keep data classes or models if needed
-keepclassmembers class * {
    @androidx.room.* <fields>;
    @androidx.room.* <methods>;
}
