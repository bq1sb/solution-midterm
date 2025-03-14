
enum CoffeeSize {
    Small(1.0), medium(1.5), large(2.0);
    private final double priceMultiplier;
    CoffeeSize(double multiplier) {this.priceMultiplier = multiplier;}
    public double getMultiplier() {return priceMultiplier;}
}
