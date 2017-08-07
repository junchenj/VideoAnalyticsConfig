package analysis;


public class FileManager {

	public static boolean localOrRemote = 
			System.getProperty("os.name").equals("Mac OS X");
	public static String sep = localOrRemote ? "/" 
//			: "\\";
			: "/";
	public static final String FormatSep = "\t";
	
	public static String RootExternallySet = null;
	
	public static String Root(){
		if (RootExternallySet != null) return RootExternallySet;
		return localOrRemote ? 
				"/Users/junchenjiang/Documents/research/ddn-controller/data/datasets/"
//				: "D:\\DdnData\\datasets\\";
//				: RootExternallySet;
				: "/users/junchenj/backend/";
	}
	
	public static String VevoRoot() {return Root()+"video-vevo"+sep;}
	public static String RawDataSchemaVevo() {
		return Root()+"schema"+sep+"raw-schema-vevo.txt";}
	public static String FormatDataSchemaVevo() {
		return Root()+"schema"+sep+"format-schema-vevo.txt";}
	
	public static String NgcRoot() {return Root()+"skype-ngc"+sep;}
	public static String RawDataSchemaNgc() {
		return Root()+"schema"+sep+"raw-schema-ngc.txt";}
	public static String FormatDataSchemaNgc() {
		return Root()+"schema"+sep+"format-schema-ngc.txt";}

	public static String BingRoot() {return Root()+"bing"+sep;}
	public static String RawDataSchemaBing() {
		return Root()+"schema"+sep+"raw-schema-bing.txt";}
	public static String FormatDataSchemaBing() {
		return Root()+"schema"+sep+"format-schema-bing.txt";}

	public static String ViacomRoot() {return Root()+"video-viacom"+sep;}
	public static String RawDataSchemaViacom() {
		return Root()+"schema"+sep+"raw-schema-viacom.txt";}
	public static String FormatDataSchemaViacom() {
		return Root()+"schema"+sep+"format-schema-viacom.txt";}

	public static String CacheFolder() {
		return Root()+"Cache"+sep;
	}

}
