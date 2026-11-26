# Le pont JavaScript doit conserver ses méthodes annotées (sinon R8 les renomme et le web ne les trouve plus).
-keepclassmembers class com.hiklon.lubaai.bridge.LubaBridge {
    @android.webkit.JavascriptInterface <methods>;
}
-keepattributes JavascriptInterface
