import yaml
try:
    yaml.safe_load("""
    run: echo "Build SHA: ${{ github.sha }}"
    """)
    print("Success")
except Exception as e:
    print("Error:", e)
