<script lang="ts">
  import { goto } from '$app/navigation'
  import { resolve } from '$app/paths'
  import GroupsScreen from '$lib/screens/GroupsScreen.svelte'
</script>

<GroupsScreen onVolver={() => void goto(resolve('/ajustes'))} />
