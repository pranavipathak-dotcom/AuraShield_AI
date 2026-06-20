import os

base_path = r"D:\AuraShield_AI\dataset\archive\for-2sec\for-2seconds"

for split in ["training", "validation", "testing"]:
    for label in ["fake", "real"]:

        path = os.path.join(base_path, split, label)

        files = [
            f for f in os.listdir(path)
            if f.endswith(".wav")
        ]

        print(f"{split}/{label} -> {len(files)} files")