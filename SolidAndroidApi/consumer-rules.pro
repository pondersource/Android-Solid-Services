-keepattributes InnerClasses,EnclosingMethod

# JJWT uses ServiceLoader to discover implementations at runtime.
# Keep the registered service classes so they survive shrinking.
-keep class io.jsonwebtoken.impl.compression.DeflateCompressionAlgorithm { <init>(); }
-keep class io.jsonwebtoken.impl.compression.GzipCompressionAlgorithm { <init>(); }
-keep class io.jsonwebtoken.orgjson.io.OrgJsonSerializer { <init>(); }
-keep class io.jsonwebtoken.orgjson.io.OrgJsonDeserializer { <init>(); }

# BouncyCastle provider entry point and EC algorithm engines used by JJWT for DPoP signing.
-keep class org.bouncycastle.jce.provider.BouncyCastleProvider { *; }
-keep class org.bouncycastle.jcajce.provider.asymmetric.ec.** { *; }
-keep class org.bouncycastle.jcajce.provider.asymmetric.util.** { *; }
-keepclassmembers class org.bouncycastle.** { public protected *; }
-dontwarn org.bouncycastle.**
