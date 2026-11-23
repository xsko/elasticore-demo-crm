package io.elasticore.demo.crm.adapter.in.web;

import io.elasticore.demo.crm.application.port.in.MemberUseCase;
import io.elasticore.springboot3.security.PasswordRefreshFilter;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * A signed-in Member's session follows the row: @signin(refresh=password).
 *
 * Before each request whose session holds a signed-in Member, the row is
 * read again, once, through signedInRow(). When it is gone (deleted, or kept
 * under @deleted), the session ends and the request goes on as a visitor's.
 * When its role has changed, the session takes the new role and keeps its id.
 * When its password has changed since the session was opened, the session
 * ends; the session that changed it keeps going.
 *
 * Regenerated with the model.
 */
@Component
public class MemberSessionRefresh extends PasswordRefreshFilter {

	private final MemberUseCase service;

	public MemberSessionRefresh(MemberUseCase service) {
		this.service = service;
	}

	@Override
	protected Optional<PasswordRefreshFilter.Row> signedInRow() {
		return service.signedInRow();
	}
}
