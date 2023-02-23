package com.example.springjwt.controllers;

public class Product {
    private String name;

    private String price;
    private String imageUrl;
	private String hyperlink;
	private String seller;
	
	
	public String getSeller() {
		return seller;
	}

	public void setSeller(String seller) {
		this.seller = seller;
	}

	
    
    public String getHyperlink() {
		return hyperlink;
	}

	public void setHyperlink(String hyperlink) {
		this.hyperlink = hyperlink;
	}


    
    public String getImageUrl() {
		return imageUrl;
	}

	public void setImageUrl(String imageUrl) {
		this.imageUrl = imageUrl;
	}

	// Constructors
    public Product() {}
    
    public Product(String name, String price, String imageUrl,String hyperlink,String seller) {
        this.name = name;
        this.imageUrl=imageUrl;
        this.price = price;
        this.hyperlink=hyperlink;
        this.seller=seller;
    }
    
    // Getters and setters
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
  
    public String getPrice() {
        return price;
    }
    
    public void setPrice(String price) {
        this.price = price;
    }

}
