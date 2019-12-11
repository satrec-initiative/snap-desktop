package org.esa.snap.ui;

/**
 * Defaults constants which may vary between packages: SeaDAS and SNAP.
 *
 * @author Daniel Knowles (NASA)
 * @version $Revision$ $Date$
 */



public class PackageDefaults extends PackageDefaultsSnap {

    public static final String COLOR_SPELLING = "Color";

    public static final String COLOR_MANIPULATION_NAME = "Color Manager";
    public static final String COLOR_MANIPULATION_MODE = "properties";
    public static final int COLOR_MANIPULATION_POSITION = 10;
    public static final boolean COLOR_MANIPULATION_OPEN = true;

    public static final String PRODUCT_EXPLORER_NAME = "File Manager";
    public static final String PRODUCT_EXPLORER_MODE = "explorer";
    public static final int PRODUCT_EXPLORER_POSITION = 10;
    public static final boolean PRODUCT_EXPLORER_OPEN = true;

    public static final String MASK_MANAGER_NAME = "Mask Manager";
    public static final String MASK_MANAGER_MODE = "properties";
    public static final int MASK_MANAGER_POSITION = 20;
    public static final boolean MASK_MANAGER_OPEN = true;

    public static final String LAYER_MANAGER_NAME = "Layer Manager";
    public static final String LAYER_MANAGER_MODE = "properties";
    public static final int LAYER_MANAGER_POSITION = 30;
    public static final boolean LAYER_MANAGER_OPEN = true;




}























//    // The spelling of the word "colour"
//    private static final String COLOR_SPELLING_SNAP = "Colour";
//    private static final String COLOR_SPELLING_SEADAS = "Color";
//
//    // The name of the Color Manipulation Tool
//    private static final String COLOR_MANIPULATION_NAME_SNAP = "Colour Manipulation";
//    private static final String COLOR_MANIPULATION_NAME_SEADAS = "Color Manager";
//
//    // The name of the Product Explorer Tool
//    private static final String PRODUCT_EXPLORER_NAME_SNAP = "Product Explorer";
//    private static final String PRODUCT_EXPLORER_NAME_SEADAS = "File Manager";
//
//
//
////
////    public static final String COLOR_SPELLING = COLOR_SPELLING_SNAP;
////    public static final String COLOR_MANIPULATION_NAME = COLOR_MANIPULATION_NAME_SNAP;
////    public static final String PRODUCT_EXPLORER_NAME = PRODUCT_EXPLORER_NAME_SNAP;
//
//
////    public static final String COLOR_SPELLING = COLOR_SPELLING_SEADAS;
////    public static final String COLOR_MANIPULATION_NAME = COLOR_MANIPULATION_NAME_SEADAS;
////    public static final String PRODUCT_EXPLORER_NAME = PRODUCT_EXPLORER_NAME_SEADAS;
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//    public enum PACKAGE {
//        SEADAS,
//        SNAP
//    }
//
//    public static final PACKAGE THIS_PACKAGE = PackageDefaults.PACKAGE.SEADAS;
//
//
//
//    enum Colors {
//        AMERICAN,
//        EUROPEAN
//    }
//
//
//    public static final int REGION_EUROPEAN = 1;
//    public static final int REGION_AMERICAN = 2;
//    public static final int REGION_DEFAULT = REGION_EUROPEAN;
//
//
//
//
//
//
//
//    private static final String[] COLOR = {
//            "Colour",
//            "Color"
//    };
//
//
//    public static final String getColor() {
//        return COLOR_SPELLING;
//    }
//
//
//    public String getColor(int region) {
//        switch (region) {
//            case REGION_EUROPEAN:
//                return COLOR[REGION_EUROPEAN];
//            case REGION_AMERICAN:
//                return COLOR[REGION_AMERICAN];
//            default:
//                return COLOR[REGION_DEFAULT];
//        }
//    }
//
//
//
//
//}
