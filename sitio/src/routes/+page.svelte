<script lang="ts">
  /* La raíz sirve la pantalla de Herencia directamente: evitar redirecciones
     en un sitio estático (el redirect de load no conserva el base y termina
     fuera de /umafinidad/). */
  import CompatScreen from '$lib/screens/CompatScreen.svelte'
</script>

<CompatScreen />
