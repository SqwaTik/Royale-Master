-dontshrink
-dontoptimize
-dontpreverify
-dontnote
-forceprocessing
-useuniqueclassmembernames
-overloadaggressively
-allowaccessmodification

-keepattributes Exceptions,InnerClasses,Signature,Deprecated,SourceFile,LineNumberTable,*Annotation*,EnclosingMethod,Record,PermittedSubclasses
-adaptclassstrings **
-adaptresourcefilecontents fabric.mod.json,mixins.json

-keep class royale.Initialization { *; }
-keep class royale.util.mods.config.wave.WaveCapesConfigOverride { *; }
-keep class royale.modules.impl.render.CustomBar { *; }
-keep class royale.mixin.** { *; }

-keepclassmembers class * {
    @org.spongepowered.asm.mixin.Shadow *;
    @org.spongepowered.asm.mixin.Unique *;
    @org.spongepowered.asm.mixin.Final *;
    @org.spongepowered.asm.mixin.Mutable *;
}

-keepclassmembers class * {
    @org.spongepowered.asm.mixin.injection.Inject *;
    @org.spongepowered.asm.mixin.injection.Redirect *;
    @org.spongepowered.asm.mixin.injection.ModifyArg *;
    @org.spongepowered.asm.mixin.injection.ModifyArgs *;
    @org.spongepowered.asm.mixin.injection.ModifyConstant *;
    @org.spongepowered.asm.mixin.injection.ModifyVariable *;
    @com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation *;
}

-dontwarn
-ignorewarnings
