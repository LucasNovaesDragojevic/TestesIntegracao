package br.com.caelum.pm73.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.hibernate.Session;
import org.junit.Test;

import br.com.caelum.pm73.dominio.Usuario;

public class UsuarioDaoTest {

	@Test
	public void deveEncontrarPeloNomeEEmail() {
		Session session = new CriadorDeSessao().getSession();
		
		UsuarioDao usuarioDao = new UsuarioDao(session);
		
		Usuario novoUsuario = new Usuario("Tobias", "tobias@email.com");
		
		usuarioDao.salvar(novoUsuario);
		
		Usuario usuario = usuarioDao.porNomeEEmail("Tobias", "tobias@email.com");
		
		assertEquals("Tobias", usuario.getNome());
		assertEquals("tobias@email.com", usuario.getEmail());
		
		session.close();
	}
	
	@Test
	public void deveRetornarNulloCasoNaoEncontreUsuario() {
		Session session = new CriadorDeSessao().getSession();
		
		UsuarioDao usuarioDao = new UsuarioDao(session);
		
		Usuario usuario = usuarioDao.porNomeEEmail("Cleidi", "cleidi@email.com");
		
		assertNull(usuario);
		
		session.close();
	}
	
}
