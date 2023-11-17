# Spring Web Session

## Keywords

- EnableRedisHttpSession
- SessionAttributes
- ModelAttribute
- SessionStatus

```java
@SessionAttributes("room")
@Controller
@RequestMapping("/room")
@RequiredArgsConstructor
public class RoomController {

	private final RoomService service;

	@GetMapping
	public String index(Model model) {
		model.addAttribute("rooms", service.findAll());
		return "room/index";
	}

	@GetMapping("/{name}")
	public String view(Model model, @PathVariable String name) {
		model.addAttribute("room", service.find(name));
		return "room/view";
	}

	@PostMapping
	public RedirectView save(
	  @ModelAttribute Room room,
	  RedirectAttributes attributes
	) {
		Room saved = service.save(room);
		attributes.addFlashAttribute("room", saved);
		return new RedirectView("room/view/" + saved.getName());
	}

	@DeleteMapping("/{name}")
	public ResponseEntity<Void> delete(
	  @PathVariable String name,
	  SessionStatus sessionStatus
	) {
		service.delete(name);

		sessionStatus.setComplete();
		return ResponseEntity.status(HttpStatus.NO_CONTENT)
		  .build();
	}
}

```

## Reference

[baeldung - Spring MVC SessionAttributes](https://www.baeldung.com/spring-mvc-session-attributes)
[baeldung - Spring Session](https://www.baeldung.com/spring-session)
[Spring doc - SessionAttributes](https://docs.spring.io/spring-framework/docs/current/reference/html/web.html#mvc-ann-sessionattributes)
