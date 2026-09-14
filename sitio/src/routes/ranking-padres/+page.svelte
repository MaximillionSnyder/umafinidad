<script lang="ts">
  import { goto } from '$app/navigation'
  import { resolve } from '$app/paths'
  import RankingScreen from '$lib/screens/RankingScreen.svelte'
</script>

<RankingScreen modoInicial="PADRES" onVolver={() => void goto(resolve('/ajustes'))} />
