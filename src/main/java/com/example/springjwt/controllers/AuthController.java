package com.example.springjwt.controllers;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.example.springjwt.models.ERole;
import com.example.springjwt.models.Role;
import com.example.springjwt.models.User;
import com.example.springjwt.payload.request.LoginRequest;
import com.example.springjwt.payload.request.SignupRequest;
import com.example.springjwt.payload.response.JwtResponse;
import com.example.springjwt.payload.response.MessageResponse;
import com.example.springjwt.repository.RoleRepository;
import com.example.springjwt.repository.UserRepository;
import com.example.springjwt.security.jwt.JwtUtils;
import com.example.springjwt.security.services.UserDetailsImpl;


@CrossOrigin
@RestController
@RequestMapping("/api/auth")
public class AuthController {
	@Autowired
	AuthenticationManager authenticationManager;

	@Autowired
	UserRepository userRepository;

	@Autowired
	RoleRepository roleRepository;

	@Autowired
	PasswordEncoder encoder;

	@Autowired
	JwtUtils jwtUtils;
	
	
	
//	
//	@GetMapping("/products")
//	public ResponseEntity<List<Product>> getProducts() {
//	    // fetch products from database or some other source
//	    List<Product> products = productService.getAllProducts();
//	    return new ResponseEntity<>(products, HttpStatus.OK);
//}

	
//	
//    @GetMapping("/products")
//    public com.example.springjwt.controllers.Product searchProduct(@RequestParam("searchTerm") String searchTerm) {
//    	RestTemplate rt = new RestTemplate();
//    	System.out.println("AAAAAAAA");
//    	System.out.println(searchTerm);
//    	String url = new String("http://localhost:8001/api/auth/search?searchTerm=" + searchTerm);
//  
//    	//String url = new String("http://localhost:8001/api/auth/search"+searchTerm);
//    	System.out.println();
//    	Product pr = rt.getForObject(url, Product.class);
//    	return pr;
//    }
    
	
//	@GetMapping("/products")
//	public com.example.springjwt.controllers.Product searchProduct(@RequestParam("searchTerm") String searchTerm) {
//	    RestTemplate rt = new RestTemplate();
//	    System.out.println("AAAAAAAA");
//	   // String searhTerm="iphone13";
//	    System.out.println(searchTerm);
//	    String url = new String("http://localhost:8001/api/auth/search/"+searchTerm);
//	    System.out.println(url);
//	    
//	    Product pr = rt.getForObject(url, Product.class);
//	    System.out.println("BBBBBBBBBBBB");
//	    return pr;
//	}
//	
	
	
	
	@GetMapping("/products")
	public List<Product> searchProduct(@RequestParam("searchTerm") String searchTerm) {
		RestTemplate restTemplate = new RestTemplate();

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);

		HttpEntity<String> entity = new HttpEntity<>("parameters", headers);
	    String url = new String("http://localhost:8001/api/auth/search/"+searchTerm);
	
	    
	    ResponseEntity<List<Product>> response = restTemplate.exchange(
	            url,
	            HttpMethod.GET,
	            entity,
	            new ParameterizedTypeReference<List<Product>>() {});

	    List<Product> products = response.getBody();
	    return products;
	}
	
	
	
//	@GetMapping("/products")
//	public com.example.springjwt.controllers.Product searchProduct(@RequestParam("searchTerm") String query) {
//	    RestTemplate rt = new RestTemplate();
//	    System.out.println("AAAAAAAA");
//	    System.out.println(query);
//	    String url = new String("http://localhost:8001/api/auth/search/"+query);
//	    System.out.println(url);
//	    Product pr = rt.getForObject(url, Product.class);
//	    System.out.println("BBBBBBBBBBBB");
//	    return pr;
//	}
	

	
	

	@PostMapping("/signin")
	public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {

		Authentication authentication = authenticationManager.authenticate(
				new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

		SecurityContextHolder.getContext().setAuthentication(authentication);
		String jwt = jwtUtils.generateJwtToken(authentication);
		
		UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();		
		List<String> roles = userDetails.getAuthorities().stream()
				.map(item -> item.getAuthority())
				.collect(Collectors.toList());

		return ResponseEntity.ok(new JwtResponse(jwt, 
												 userDetails.getId(), 
												 userDetails.getUsername(), 
												 userDetails.getEmail(), 
												 roles));
	}

	@PostMapping("/signup")
	public ResponseEntity<?> registerUser(@Valid @RequestBody SignupRequest signUpRequest) {
		if (userRepository.existsByUsername(signUpRequest.getUsername())) {
			return ResponseEntity
					.badRequest()
					.body(new MessageResponse("Error: Username is already taken!"));
		}

		if (userRepository.existsByEmail(signUpRequest.getEmail())) {
			return ResponseEntity
					.badRequest()
					.body(new MessageResponse("Error: Email is already in use!"));
		}

		// Create new user's account
		User user = new User(signUpRequest.getUsername(), 
							 signUpRequest.getEmail(),
							 encoder.encode(signUpRequest.getPassword()));

		Set<String> strRoles = signUpRequest.getRole();
		Set<Role> roles = new HashSet<>();

		if (strRoles == null) {
			Role userRole = roleRepository.findByName(ERole.ROLE_USER)
					.orElseThrow(() -> new RuntimeException("Error: Role is not found."));
			roles.add(userRole);
		} else {
			strRoles.forEach(role -> {
				switch (role) {
				case "admin":
					Role adminRole = roleRepository.findByName(ERole.ROLE_ADMIN)
							.orElseThrow(() -> new RuntimeException("Error: Role is not found."));
					roles.add(adminRole);

					break;
				case "mod":
					Role modRole = roleRepository.findByName(ERole.ROLE_MODERATOR)
							.orElseThrow(() -> new RuntimeException("Error: Role is not found."));
					roles.add(modRole);

					break;
				default:
					Role userRole = roleRepository.findByName(ERole.ROLE_USER)
							.orElseThrow(() -> new RuntimeException("Error: Role is not found."));
					roles.add(userRole);
				}
			});
		}

		user.setRoles(roles);
		userRepository.save(user);

		return ResponseEntity.ok(new MessageResponse("User registered successfully!"));
	}
}
