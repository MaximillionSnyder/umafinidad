<script lang="ts">
  import { goto } from '$app/navigation'
  import { resolve } from '$app/paths'
  import TopLinajesScreen from '$lib/screens/TopLinajesScreen.svelte'
</script>

<TopLinajesScreen onVolver={() => void goto(resolve('/ajustes'))} />
