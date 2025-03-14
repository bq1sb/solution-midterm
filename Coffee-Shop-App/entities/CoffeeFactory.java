class CoffeeFactory{
    public static Coffee createCoffee(String type, CoffeeSize size){
        return switch (type.toLowerCase()){
            case "espresso" -> new Espresso(size);
            case "cappuccino" -> new Cappuccino(size);
            default -> throw new illegalArgumentException("Unknown coffee type")
        };
    }
}