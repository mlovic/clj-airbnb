f = File.read('../abntest')
md = f.scan(/\/rooms\/([0-9]+)/)
p md.flatten.uniq.map(&:to_i)
