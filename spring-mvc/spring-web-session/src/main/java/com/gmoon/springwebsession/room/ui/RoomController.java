package com.gmoon.springwebsession.room.ui;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;

import com.gmoon.springwebsession.room.application.RoomService;
import com.gmoon.springwebsession.room.domain.Room;

import lombok.RequiredArgsConstructor;

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
